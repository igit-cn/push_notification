package com.yidian.push.stats;


import com.mongodb.BasicDBObject;
import org.testng.annotations.Test;

public class DailyStatTest {

    @Test
    public void testUpdate() throws Exception {
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.put("1", "1");
        BasicDBObject subBasicDBObject = new BasicDBObject();
        subBasicDBObject.put("2", "sub");
        basicDBObject.put("2", subBasicDBObject);
        DailyStat.update("2.3", "3", basicDBObject);
        System.out.println(basicDBObject);
        DailyStat.update("2.2", "new_2", basicDBObject);
        System.out.println(basicDBObject);
    }
}