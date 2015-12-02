package com.yidian.serving.metrics.reporter.opentsdb;

import java.util.Map;

/**
 * @author jixu
 */
public class OpenTsdbMetric {
    private final String metric;
    private final Long timestamp;
    private final Number value;
    private final Map<String, String> tags;

    public OpenTsdbMetric(String metric, Long timestamp, Number value, Map<String, String> tags) {
        this.metric = metric;
        this.timestamp = timestamp;
        this.value = value;
        this.tags = tags;
    }

    public String getMetric() {
        return metric;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Number getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return "OpenTsdbMetric->metric: " + metric + ", value: " + value
                + ", timestamp: " + timestamp + ", tags: " + tags;
    }
}
