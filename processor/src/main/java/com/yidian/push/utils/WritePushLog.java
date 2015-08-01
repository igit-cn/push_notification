package com.yidian.push.utils;

import com.yidian.push.config.Config;
import com.yidian.push.config.ProcessorConfig;
import com.yidian.push.data.HostPort;
import com.yidian.push.data.Platform;
import com.yidian.push.data.PushLog;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Created by tianyuzhi on 15/7/28.
 */
@Log4j
public class WritePushLog {
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
        HostPort hostPort = config.getLogger(platform);
        writeLog(logItemList, hostPort, config.getSocketConnectTimeout(), config.getSocketReadTimeout());
    }

   public static void writeLog(List<PushLog.LogItem> logItemList,
                               HostPort hostPort, int connectionTimeout, int readTimeout) throws IOException {
       if (null == logItemList || logItemList.size() == 0) {
           return;
       }

       Socket client = new Socket();
       try {
           log.info("start to write # logs : " + logItemList.size());

           client.setTcpNoDelay(true);
           client.setSoTimeout(readTimeout * 1000);
           client.connect(new InetSocketAddress(hostPort.getHost(), hostPort.getPort()), connectionTimeout * 1000);
           InputStream in = client.getInputStream();
           DataOutputStream out = new DataOutputStream(client.getOutputStream());
           for (PushLog.LogItem logItem : logItemList) {
               byte[] bytes = PushLog.encodeLogItem(logItem);
               out.write(bytes);
           }
           out.write(new byte[1], 0, 1);
           in.read();
           log.info("write # logs : " + logItemList.size());
       } finally {
           if (null != client) { try {client.close();} catch (IOException ignore){}}
       }
   }
}
