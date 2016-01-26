package com.yidian.push.weather.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by tianyuzhi on 16/1/20.
 */
@Getter
@Setter
public class Document {
    private Alarm alarm;
    private String alarmId;
    private String docId;
    private String title;
    private String content;
    private String publishTime;
    private boolean shouldPush = false;
    private boolean pushed = false;
    private Map<String, Boolean> fromIdPushed = new HashMap<>();
    private Set<String> areas = new HashSet<>();

    public Document(){}


    public Document(Alarm alarm) {
        this.alarm = alarm;
        this.alarmId = alarm.getId();
        this.title = alarm.getProvince() + alarm.getCity() + alarm.getCounty()
                + "气象台发布" + alarm.getCategoryName() + alarm.getLevelName()
                + "预警";
        this.content = alarm.getContent();
        this.publishTime = alarm.getPublishTime();
    }

    public void addArea(String area) {
        if (areas == null) {
            areas = new HashSet<>();
        }
        if (StringUtils.isNotEmpty(area)) {
            areas.add(area);
        }
    }
    public void addFromId(String fromId) {
        if (null == fromIdPushed) {
            fromIdPushed = new HashMap<>();
        }
        if (StringUtils.isNotEmpty(fromId)
                && !fromIdPushed.containsKey(fromId)) {
            fromIdPushed.put(fromId, false);
        }
    }

    public void markFromIdAsPushed(String fromId) {
        if (fromIdPushed != null
                && StringUtils.isNotEmpty(fromId)
                //&& fromIdPushed.containsKey(fromId)
                ) {
            fromIdPushed.put(fromId, true);

        }
    }

    public void markAsPushed() {
        pushed = true;
    }
}
