package com.yidian.push.weather.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 16/1/20.
 */
@Getter
@Setter
public class Document {
    private Alarm alarm;
    private String docId;
    private String title;
    private String content;


    public Document(Alarm alarm) {
        this.alarm = alarm;
        this.title = alarm.getProvince() + alarm.getCity() + alarm.getCounty()
                + "气象台发布" + alarm.getCategoryName() + alarm.getLevelName()
                + "预警";
        this.content = alarm.getContent();
    }
}
