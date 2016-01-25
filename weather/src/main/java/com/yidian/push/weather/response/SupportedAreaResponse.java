package com.yidian.push.weather.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/25.
 */
@Getter
@Setter
public class SupportedAreaResponse extends Response {
    private Map<String, String> result;
}
