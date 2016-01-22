package com.yidian.push.weather.util;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.yidian.push.utils.GsonFactory;
import org.bson.Document;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Created by tianyuzhi on 16/1/22.
 */
public class MongoUtilTest {

    @Test
    public void test() {
        MongoClient mongoClient = new MongoClient("10.101.2.22");
        MongoDatabase db = mongoClient.getDatabase("instant_weather_alarm");
        MongoCollection collection = db.getCollection("weather_alarm");
        com.yidian.push.weather.data.Document document2 = new com.yidian.push.weather.data.Document();
        Map<String, String> fromIds = new HashMap<>();
        fromIds.put("c1", "1");
        fromIds.put("c2", "1");
        fromIds.put("c3", "1");
        document2.setAlamId("test_alarm");
        document2.setDocId("test_docid");
        document2.setTitle("test_title");
        document2.setContent("test_content");
        document2.setPublishDate("date");
        document2.setFromIds(fromIds);

        Map<String, Object> args = new HashMap<>();
        args.put("docId", document2.getDocId());
        args.put("title", document2.getTitle());
        args.put("content", document2.getContent());
        args.put("fromIds", document2.getFromIds());
        Document query = new Document().append("_id", document2.getAlamId());
        collection.findOneAndUpdate(query, new Document().append("$set", args),
                new FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE).upsert(true));



        List<String> alarmIds = Arrays.asList(document2.getAlamId());
        FindIterable<Document> cursor = collection.find(Filters.in("_id", alarmIds));
        for (Document document : cursor) {
            com.yidian.push.weather.data.Document doc = new com.yidian.push.weather.data.Document();
            doc.setDocId(document.getString("docId"));
            doc.setAlamId(document.getString("_id"));
            doc.setTitle(document.getString("title"));
            doc.setContent(document.getString("content"));
            doc.setFromIds(document.get("fromIds", Map.class));
            System.out.println(GsonFactory.getNonPrettyGson().toJson(doc));
        }
        System.out.println("");
    }

}