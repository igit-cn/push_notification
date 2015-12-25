package com.yidian.push.instant.util;

import com.mongodb.InsertOptions;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yidian.push.config.Config;
import com.yidian.push.config.InstantPushConfig;
import com.yidian.push.instant.data.Channel;
import com.yidian.push.instant.data.DocChannelInfo;
import com.yidian.serving.metrics.MetricsFactoryUtil;
import lombok.extern.log4j.Log4j;
import org.bson.Document;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 15/12/24.
 */
@Log4j
public class MongoUtil {
    public static final String INSERT_MONGO_QPS = "push_notification.install_push.insert_doc.qps";
    public static volatile boolean initialized = false;
    public static MongoClient mongoClient = null;
    private static InstantPushConfig config = null;
    public static void init() throws IOException {
        config = Config.getInstance().getInstantPushConfig();
        mongoClient = new MongoClient(config.getMongoHost(), config.getMongoPort());
    }

    public static void destory() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }


    public static Document buildDocument(DocChannelInfo docChannelInfo) {
        Document document = new Document();
        String day = new DateTime(DateTimeZone.UTC).toString("yyyy-MM-dd");
        String insertTime = new DateTime(DateTimeZone.UTC).toString("yyyy-MM-dd HH:mm:ss");
        /**
         * /*
         {"signature":"de70cb68fa2c7105c1a7db0695929d7a"
         ,"name":"jingxuan"
         ,"fromId":"u22592"
         ,"r":0.7
         ,"score":0.17900006473064423},
         */
        List<Map<String, Object>> channels = new ArrayList<>();
        for (Channel channel : docChannelInfo.getChannels()) {
            Map<String, Object> map = new HashMap<>();
            map.put("signature", channel.getSignature());
            map.put("name", channel.getName());
            map.put("fromId", channel.getFromId());
            map.put("r", channel.getRelevance());
            map.put("score", channel.getScore());
            channels.add(map);
        }
        document.append("_id", docChannelInfo.getDocId())
                .append("modifiedAt", docChannelInfo.getModifiedAt())
                .append("day", day)
                .append("insertTime", insertTime)
                .append("channels", channels);
        return document;
    }
    public static boolean insertData(List<DocChannelInfo> docChannelInfoList) {
        if (null == docChannelInfoList) {
            return true;
        }
        MongoDatabase db = mongoClient.getDatabase(config.getMongoDBName());
        MongoCollection collection = db.getCollection(config.getMongoCollName());
        for (DocChannelInfo docChannelInfo : docChannelInfoList) {
            Document document = buildDocument(docChannelInfo);
            try {
                collection.insertOne(document);
            } catch (Exception e) {
                log.error("insert into mongo failed..." + e.getMessage());
            }
            log.info("MONGO_INSERT:" + docChannelInfo.getDocId());
            MetricsFactoryUtil.getRegisteredFactory().getMeter(INSERT_MONGO_QPS).mark();
        }
        return true;
    }
}
