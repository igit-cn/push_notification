package com.yidian.push.weather.util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.yidian.push.config.Config;
import com.yidian.push.config.WeatherPushConfig;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.*;

/**
 * Created by tianyuzhi on 16/1/21.
 */
@Log4j
public class MongoUtil {
    public static volatile boolean initialized = false;
    public static MongoClient mongoClient = null;
    private static WeatherPushConfig config = null;

    public static void init() throws IOException {
        if (initialized) {return;}
        synchronized (MongoUtil.class) {
            if (initialized) {return;}
            config = Config.getInstance().getWeatherPushConfig();
            mongoClient = new MongoClient(config.getMongoHost(), config.getMongoPort());
            initialized = true;
        }
    }

    public static void destroy() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public static boolean getProcessedDocuments(Collection<String> alarmIds,
                                                List<com.yidian.push.weather.data.Document> processedDocuments) {
        boolean succeed = true;
        try {
            MongoDatabase db = mongoClient.getDatabase(config.getMongoDBName());
            MongoCollection collection = db.getCollection(config.getMongoWeatherCollName());
            FindIterable<Document> cursor = collection.find(Filters.in("_id", alarmIds));
            for (Document document : cursor) {
                com.yidian.push.weather.data.Document doc = new com.yidian.push.weather.data.Document();
                doc.setDocId(document.getString("docId"));
                doc.setAlamId(document.getString("_id"));
                doc.setTitle(document.getString("title"));
                doc.setContent(document.getString("content"));
                doc.setPublishDate(document.getString("publishDate"));
                doc.setFromIds(document.get("fromIds", Map.class));
                processedDocuments.add(doc);
            }
            succeed = true;
        } catch (Exception e) {
            log.error("get processed alarm id from mongo err:" + ExceptionUtils.getFullStackTrace(e));
            succeed = false;
        }
        return succeed;
    }

    public static boolean saveDocuments(List<com.yidian.push.weather.data.Document> docs) {
        boolean succeed = false;
        try {
            MongoDatabase db = mongoClient.getDatabase(config.getMongoDBName());
            MongoCollection collection = db.getCollection(config.getMongoWeatherCollName());
            for (com.yidian.push.weather.data.Document doc : docs) {
                String lastUpdateTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
                Document query = new Document().append("_id", doc.getAlamId());
                Document update = new Document()
                        .append("docId", doc.getDocId())
                        .append("title", doc.getTitle())
                        .append("content", doc.getContent())
                        .append("publishDate", doc.getPublishDate())
                        .append("fromIds", doc.getFromIds())
                        .append("lastUpdateTime", lastUpdateTime);
                collection.findOneAndUpdate(query,
                        new Document().append("$set",update),
                        new FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE).upsert(true));
            }
            succeed = true;
        } catch (Exception e) {
            log.error("save documents failed with exception:" + ExceptionUtils.getFullStackTrace(e));
            succeed = false;
        }
        return succeed;
    }
}
