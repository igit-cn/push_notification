package com.yidian.push.weather.response;

import com.yidian.push.weather.data.Alarm;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by tianyuzhi on 16/1/25.
 */
@Getter
@Setter
public class AlarmResponse extends Response{
    private List<Alarm> result;


}
