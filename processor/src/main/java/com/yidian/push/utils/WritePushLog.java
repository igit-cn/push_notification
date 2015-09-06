package com.yidian.push.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.HostPort;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushLog;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/7/28.
 */
@Log4j
public class WritePushLog {
    private static final String SEPARATOR = "\u0001";
    public static void writeLogIgnoreException(Platform platform, List<PushLog.LogItem> logItemList) {
        try {
            writeLog(platform, logItemList);
        } catch (IOException e) {
            log.error("write logs failed for platform : " + platform + ", with exception: " + ExceptionUtils.getFullStackTrace(e));
        }
    }

    public static void writeLog(Platform platform, List<PushLog.LogItem> logItemList) throws IOException {
        if (null == logItemList || logItemList.size() == 0) {
            return;
        }
        ProcessorConfig config = Config.getInstance().getProcessorConfig();
        if (config.isNeedWriteLog()) {
            List<HostPort> hostPortList = config.getLoggerList(platform);
            try {
                writeLog(logItemList, hostPortList, config.getSocketConnectTimeout(), config.getSocketReadTimeout());
            } catch (IOException e) {
                log.error("write log failed..");
            }
        }
        if (config.isNeedWriteHistory()) {
            String historyUrl = config.getPushHistoryUrl();
            int batchSize = config.getPushHistoryBatchSize();
            int retryTimes = config.getRetryTimes();
            RequestConfig requestConfig = config.getRequestConfig();
            writeHistory(platform, logItemList, historyUrl, retryTimes, batchSize, requestConfig);
        }
    }

    public static void writeHistory(Platform platform, List<PushLog.LogItem> logItemList, String url, int retryTimes, int batchSize, RequestConfig requestConfig) {
        if (null == logItemList || logItemList.size() == 0) {
            return;
        }
        List<String> logList = new ArrayList<>(logItemList.size());
        for (PushLog.LogItem logItem : logItemList) {
            String str = new StringBuilder().append(logItem.getUid()).append(",").append(platform.getTableId())
                    .append(SEPARATOR)
                    .append(GsonFactory.getNonPrettyGson().toJson(logItem)).toString();
            logList.add(str);
        }

        int index = 0;
        int length = logList.size();
        while (index < length) {
            int start = index;
            int end = (index + batchSize) > length ? length : index + batchSize;
            index += batchSize;
            List<String> subLogs = logList.subList(start, end);
            Map<String, Object> params = new HashMap<>(10);
            params.put("record", subLogs);
            int timesToRetry = retryTimes;
            while (timesToRetry > 0) {
                try {
                    String response = HttpConnectionUtils.getPostResult(url, params, null, requestConfig);
                    JSONObject json = JSON.parseObject(response);
                    if (null != json && "success".equals(json.getString("status"))) {
                        break;
                    } else {
                        if (null != json) {
                            log.error("write push log failed with reason " + json.getString("desc"));
                        }
                        timesToRetry--;
                    }
                } catch (IOException e) {
                    log.error("write push history failed");
                    timesToRetry--;
                }
            }
        }
    }

   public static void writeLog(List<PushLog.LogItem> logItemList,
                               List<HostPort> hostPortList, int connectionTimeout, int readTimeout) throws IOException {
       if (null == logItemList || logItemList.size() == 0) {
           return;
       }
       List<byte[]> byteList = new ArrayList<>(logItemList.size());
       for (PushLog.LogItem logItem : logItemList) {
           byte[] bytes = PushLog.encodeLogItem(logItem);
           byteList.add(bytes);
       }

       for (HostPort hostPort : hostPortList) {
           Socket client = new Socket();
           try {
               log.debug("start to write # logs : " + logItemList.size());
               client.setTcpNoDelay(true);
               client.setSoTimeout(readTimeout * 1000);
               client.connect(new InetSocketAddress(hostPort.getHost(), hostPort.getPort()), connectionTimeout * 1000);
               InputStream in = client.getInputStream();
               DataOutputStream out = new DataOutputStream(client.getOutputStream());
               for (byte[] bytes : byteList) {
                   out.write(bytes);
               }
               out.write(new byte[1], 0, 1);
               in.read();
               log.debug("write # logs : " + logItemList.size());
           } finally {
               if (null != client) {
                   try {
                       client.close();
                   } catch (IOException ignore) {
                   }
               }
           }
       }
   }
}
