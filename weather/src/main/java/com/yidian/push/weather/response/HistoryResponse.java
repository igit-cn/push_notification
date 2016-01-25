package com.yidian.push.weather.response;

import com.yidian.push.weather.data.Document;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Created by tianyuzhi on 16/1/25.
 */
@Getter
@Setter
public class HistoryResponse extends Response {
    private Map<String, Document> result;
}
