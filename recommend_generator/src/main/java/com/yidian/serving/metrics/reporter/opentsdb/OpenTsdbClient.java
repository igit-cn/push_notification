package com.yidian.serving.metrics.reporter.opentsdb;

import java.util.List;

/**
 * A client to send metrics to OpenTsdb
 * @author jixu
 */
public interface OpenTsdbClient {
    public boolean send(List<OpenTsdbMetric> metrics);
}
