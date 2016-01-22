package com.yidian.push.weather.util;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.weather.data.Alarm;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
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
                doc.setAlarmId(document.getString("_id"));
                doc.setTitle(document.getString("title"));
                doc.setContent(document.getString("content"));
                doc.setPublishTime(document.getString("publishTime"));
                doc.setShouldPush(document.getBoolean("shouldPush", false));
                doc.setPushed(document.getBoolean("pushed", true));
                doc.setFromIdPushed(document.get("fromIdPushed", Map.class));
                String alarmString = document.getString("alarm");
                if (StringUtils.isNotEmpty(alarmString)) {
                    try {
                        Alarm alarm1 = GsonFactory.getNonPrettyGson().fromJson(alarmString, Alarm.class);
                        doc.setAlarm(alarm1);
                    } catch (Exception e) {
                        log.error("bad alarm from mongo " + doc.getAlarmId());
                    }
                }
                processedDocuments.add(doc);
            }
            succeed = true;
        } catch (Exception e) {
            log.error("get processed alarm id from mongo err:" + ExceptionUtils.getFullStackTrace(e));
            succeed = false;
        }
        return succeed;
    }

    public static boolean saveOrUpdateDocuments(List<com.yidian.push.weather.data.Document> docs) {
        boolean succeed = false;
        try {
            MongoDatabase db = mongoClient.getDatabase(config.getMongoDBName());
            MongoCollection collection = db.getCollection(config.getMongoWeatherCollName());
            for (com.yidian.push.weather.data.Document doc : docs) {
                String lastUpdateTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
                Document query = new Document().append("_id", doc.getAlarmId());
                Document update = new Document()
                        .append("docId", doc.getDocId())
                        .append("title", doc.getTitle())
                        .append("content", doc.getContent())
                        .append("publishTime", doc.getPublishTime())
                        .append("shouldPush", doc.isShouldPush())
                        .append("pushed", doc.isPushed())
                        .append("fromIdPushed", doc.getFromIdPushed())
                        .append("alarm", GsonFactory.getNonPrettyGson().toJson(doc.getAlarm()))
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
