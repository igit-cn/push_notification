package com.yidian.push.instant.consumer;

import com.hipu.relevance.core.query.Query;
import com.yidian.push.instant.data.DocChannelInfo;
import com.yidian.push.instant.util.FilterUtil;
import com.yidian.push.utils.GsonFactory;
import com.yidian.serving.metrics.MetricsFactoryUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Log4j
@Getter
public class FilterThread extends Thread {
    public static final String MATCHED_QPS = "push_notification.install_push.matched_doc.qps";
    private String threadName = "";
    private BlockingQueue<String> docInfoStringQueue = null;
    private BlockingQueue<DocChannelInfo> docChannelInfoQueue = null;
    private int fetchSize = 1;
    private String queryFile = null;
    private List<Query> queryList;
    private Map<String, List<Query>> tagToQueries = null;

    public FilterThread(String name, BlockingQueue<String> docInfoStringQueue,
                        BlockingQueue<DocChannelInfo> docChannelInfoLinkedBlockingQueue,
                        int fetchSize, String queryFile) {
        super(name);
        this.threadName = name;
        this.docInfoStringQueue = docInfoStringQueue;
        this.docChannelInfoQueue = docChannelInfoLinkedBlockingQueue;
        this.fetchSize = fetchSize;
        this.queryFile = queryFile;
        this.queryList = FilterUtil.loadQueries(this.queryFile);
        this.tagToQueries = FilterUtil.loadTagQueries(this.queryFile);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<String>  docInfoList = new ArrayList<>(fetchSize);
            int num = docInfoStringQueue.drainTo(docInfoList, fetchSize);
            if (num <= 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            log.info(Thread.currentThread().isInterrupted());

            List<DocChannelInfo> docChannelInfoList = new ArrayList<>(num);
            for (String str : docInfoList) {
                try {
                    DocChannelInfo docChannelInfo = GsonFactory.getDefaultGson().fromJson(str, DocChannelInfo.class);
                    docChannelInfoList.add(docChannelInfo);
                } catch (Exception e) {
                    log.error("could not parse the doc channel info : " + str);
                }
            }
            List<DocChannelInfo> matchedList = FilterUtil.matchQueries(tagToQueries, docChannelInfoList);
            for (DocChannelInfo docChannelInfo : matchedList) {
                docChannelInfoQueue.add(docChannelInfo);
                MetricsFactoryUtil.getRegisteredFactory().getMeter(MATCHED_QPS).mark();
            }
        }
    }
}
