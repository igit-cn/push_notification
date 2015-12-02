package com.yidian.serving.metrics;

import com.yidian.serving.metrics.core.Gauge;
import com.yidian.serving.metrics.core.Histogram;
import com.yidian.serving.metrics.core.Meter;

/**
 * To get Histogram and Meter objects based on metric name.
 * Also, Gauge can be registered in the MetricsFactory
 * @author jixu
 */
public interface MetricsFactory {
    public Histogram getHistogram(String name);
    public Meter getMeter(String name);
    public <T extends Number> boolean register(Gauge<T> gauge);
}
