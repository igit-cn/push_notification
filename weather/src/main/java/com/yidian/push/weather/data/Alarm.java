package com.yidian.push.weather.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tianyuzhi on 16/1/19.
 */
@Getter
@Setter
public class Alarm {
    private String province;
    private String city;
    private String county;
    private String categoryId;
    private String categoryName;
    private String levelId;
    private String levelName;
    private String publishTime;
    private String content;
    private String id;
}
