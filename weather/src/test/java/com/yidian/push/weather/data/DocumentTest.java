package com.yidian.push.weather.data;

import org.testng.annotations.Test;


/**
 * Created by tianyuzhi on 16/2/15.
 */
public class DocumentTest {
    @Test
    public void testDocument() {
        Alarm alarm = new Alarm();
        alarm.setProvince("province");
        alarm.setCity("city");
        alarm.setCounty("county");
        alarm.setContent("content");
        Document document = new Document(alarm);
        assert !document.getTitle().contains("province");
    }

    @Test
    public void testDocument2() {
        Alarm alarm = new Alarm();
        alarm.setProvince("province");
        alarm.setCity("");
        alarm.setCounty("");
        alarm.setContent("content");
        Document document = new Document(alarm);
        assert document.getTitle().contains("province");
    }

    @Test
    public void testDocumentTitle() {
        Alarm alarm = new Alarm();
        alarm.setProvince("province");
        alarm.setCity("same");
        alarm.setCounty("same");
        alarm.setContent("content");
        Document document = new Document(alarm);
        assert 0 == document.getTitle().indexOf("same", 0);
        assert -1 == document.getTitle().indexOf("same", 2);

        alarm = new Alarm();
        alarm.setProvince("same");
        alarm.setCity("");
        alarm.setCounty("same");
        alarm.setContent("content");
        document = new Document(alarm);
        assert 0 == document.getTitle().indexOf("same", 0);
        assert -1 == document.getTitle().indexOf("same", 2);
    }

}