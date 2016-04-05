package com.yidian.push.weather.data;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by tianyuzhi on 16/1/20.
 */
@Getter
@Setter
public class Document {
    private static final String WARNING = "预警";

    private Alarm alarm;
    private String alarmId;
    private String docId;
    private String title;
    private String content;
    private String publishTime;
    private boolean shouldPush = false;
    private Sound sound = Sound.NO_SOUND;
    private boolean pushed = false;
    private Map<String, Boolean> fromIdPushed = new HashMap<>();
    private Map<String, String> areas = new HashMap<>();

    public Document(){}

    public Document(Alarm alarm) {
        this.alarm = alarm;
        this.alarmId = alarm.getId();
        List<String> contents = null;
        if (StringUtils.isNotEmpty(alarm.getCity())) {
            if (StringUtils.equals(alarm.getCity(), alarm.getCounty())) {
                contents = Arrays.asList(alarm.getCity(), "气象台发布", alarm.getCategoryName(), alarm.getLevelName(), WARNING );
            }
            else {
                contents = Arrays.asList(alarm.getCity(), alarm.getCounty(), "气象台发布", alarm.getCategoryName(), alarm.getLevelName(), WARNING );
            }
        }
        else {
            if (StringUtils.equals(alarm.getProvince(), alarm.getCounty())) {
                contents = Arrays.asList(alarm.getProvince(), "气象台发布", alarm.getCategoryName(), alarm.getLevelName(), WARNING );
            }
            else {
                contents = Arrays.asList(alarm.getProvince(), alarm.getCounty(), "气象台发布", alarm.getCategoryName(), alarm.getLevelName(), WARNING );
            }
        }
        this.title = StringUtils.join(contents, "");
        this.content = alarm.getContent();
        this.publishTime = alarm.getPublishTime();
    }

    public void addArea(String area, String fromId) {
        if (areas == null) {
            areas = new HashMap<>();
        }
        if (StringUtils.isNotEmpty(area)) {
            areas.put(fromId, area);
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
