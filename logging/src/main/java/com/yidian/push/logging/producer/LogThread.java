package com.yidian.push.logging.producer;

import com.alibaba.fastjson.JSONObject;
import com.yidian.push.data.Platform;
import com.yidian.push.push_request.PushRecord;
import com.yidian.push.push_request.PushRequest;
import com.yidian.push.push_request.PushRequestManager;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/8/25.
 */
@Log4j
@Getter
public class LogThread extends Thread {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private int threadId;
    private BlockingQueue<PushRequest> pushRequestQueue;
    private ProducerConfig producerConfig;
    private Producer<Integer, String> producer;
    private String topicName;

    public LogThread(int threadId, BlockingQueue<PushRequest> pushRequestQueue, Properties producerProperties, String topicName) {
        this.threadId = threadId;
        this.pushRequestQueue = pushRequestQueue;
        this.producerConfig = new ProducerConfig(producerProperties);
        this.producer = new Producer<>(producerConfig);
        this.topicName = topicName;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            PushRequest request = null;
            try {
                request = pushRequestQueue.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                request = null;
            }
            if (null == request) {
                continue;
            }
            log.debug("thread " + threadId + " : " + request.getFileName());

            BufferedReader reader = null;
            try {
                Path filePath = new File(request.getFileName()).toPath();
                reader = Files.newBufferedReader(filePath, UTF_8);
                String line = null;
                String platform = Platform.tableToPlatform(request.getTable()).toString();
                String pushDay = DateTime.now().toString("yyyy-MM-dd");

                while ((line = reader.readLine()) != null) {
                    PushRecord pushRecord = new PushRecord(line);
                    if (!pushRecord.isValid()) {
                        log.error("invalid record line:" + line);
                        continue;
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("uid", pushRecord.getUid());
                    jsonObject.put("appid", pushRecord.getAppId());
                    jsonObject.put("docid", pushRecord.getDocId());
                    jsonObject.put("push_type", pushRecord.getNewsType());
                    jsonObject.put("platform", platform);
                    jsonObject.put("push_day", pushDay);
                    try {
                        producer.send(new KeyedMessage<Integer, String>(topicName, jsonObject.toString()));
                    } catch (Exception e) {
                        log.error("start a new producer for it failed with exception: " + ExceptionUtils.getFullStackTrace(e));
                        this.producer = new Producer<>(producerConfig);
                    }
                }
                PushRequestManager.getInstance().markAsLogged(request);
            } catch (IOException e) {
                log.error("write log failed with exception: " + ExceptionUtils.getFullStackTrace(e));
                try {
                    PushRequestManager.getInstance().markAsBad(request);
                } catch (IOException e1) {
                    // here we just ignore the failure
                    log.error("mark as logged failed. " + request.getFileName());
                }
                if (null != reader) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                        //ignore
                    }
                }
            }

        }

    }
}
