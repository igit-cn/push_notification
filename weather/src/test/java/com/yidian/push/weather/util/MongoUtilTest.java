package com.yidian.push.weather.util;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.yidian.push.utils.GsonFactory;
import com.yidian.push.weather.data.Alarm;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/22.
 */
public class MongoUtilTest {

    @Test
    public void test() {
        System.out.println("1[" + GsonFactory.getNonPrettyGson().toJson(null) + "]");
        Alarm alarmNull = GsonFactory.getNonPrettyGson().fromJson("null", Alarm.class);
        MongoClient mongoClient = new MongoClient("10.101.2.22");
        MongoDatabase db = mongoClient.getDatabase("instant_weather_alarm");
        MongoCollection collection = db.getCollection("weather_alarm");
        com.yidian.push.weather.data.Document document2 = new com.yidian.push.weather.data.Document();
        Alarm alarm = new Alarm();
        alarm.setCategoryId("test_cat_id");
        Map<String, Boolean> fromIds = new HashMap<>();
        fromIds.put("c1", true);
        fromIds.put("c2", false);
        fromIds.put("c3", true);
        document2.setAlarmId("test_alarm");
        document2.setDocId("test_docid");
        document2.setTitle("test_title");
        document2.setContent("test_content");
        document2.setPublishTime("date");
        document2.setFromIdPushed(fromIds);
        document2.setAlarm(alarm);

        Map<String, Object> args = new HashMap<>();
        args.put("docId", document2.getDocId());
        args.put("title", document2.getTitle());
        args.put("content", document2.getContent());
        args.put("fromIdPushed", document2.getFromIdPushed());
        args.put("shouldPush", document2.isShouldPush());
        args.put("pushed", document2.isPushed());
        args.put("alarm", GsonFactory.getNonPrettyGson().toJson(document2.getAlarm()));
        Document query = new Document().append("_id", document2.getAlarmId());
        collection.findOneAndUpdate(query, new Document().append("$set", args),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE).upsert(true));



        List<String> alarmIds = Arrays.asList(document2.getAlarmId());
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
                    // ignore
                }
            }
            System.out.println(GsonFactory.getNonPrettyGson().toJson(doc));
        }
        System.out.println("");
    }

}