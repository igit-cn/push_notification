package com.yidian.push.instant.consumer;

import com.hipu.relevance.core.query.Query;
import com.yidian.push.instant.data.DocChannelInfo;
import com.yidian.push.instant.util.FilterUtil;
import com.yidian.push.instant.util.MongoUtil;
import com.yidian.push.utils.GsonFactory;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Log4j
@Getter
public class InsertMongoThread extends Thread {
    private String threadName = "";
    private BlockingQueue<DocChannelInfo> docChannelInfoQueue = null;
    private int fetchSize = 10;


    public InsertMongoThread(String name, BlockingQueue<DocChannelInfo> docChannelInfoQueue, int fetchSize) {
        super(name);
        this.threadName = name;
        this.docChannelInfoQueue = docChannelInfoQueue;
        this.fetchSize = fetchSize;
    }

    @Override
    public void run() {
        log.info("in InsertMongoThread");
        while (!Thread.currentThread().isInterrupted()) {
            List<DocChannelInfo>  docInfoList = new ArrayList<>(fetchSize);
            int num = docChannelInfoQueue.drainTo(docInfoList, fetchSize);
            log.info("get # of DocChannelInfo : " + num);
            if (num <= 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            MongoUtil.insertData(docInfoList);

        }
    }
}
