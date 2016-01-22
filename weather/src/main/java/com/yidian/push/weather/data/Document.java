package com.yidian.push.weather.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/20.
 */
@Getter
@Setter
public class Document {
    private Alarm alarm;
    private String alamId;
    private String docId;
    private String title;
    private String content;
    private String publishDate;
    private Map<String, String> fromIds = new HashMap<>();

    public Document(){}


    public Document(Alarm alarm) {
        this.alarm = alarm;
        this.alamId = alarm.getId();
        this.title = alarm.getProvince() + alarm.getCity() + alarm.getCounty()
                + "气象台发布" + alarm.getCategoryName() + alarm.getLevelName()
                + "预警";
        this.content = alarm.getContent();
        this.publishDate = alarm.getPublishTime();
    }

    public void addFromId(String fromId) {
        if (null == fromIds) {
            fromIds = new HashMap<>();
        }
        if (StringUtils.isNotEmpty(fromId)) {
            fromIds.put(fromId, "1");
        }
    }
}
