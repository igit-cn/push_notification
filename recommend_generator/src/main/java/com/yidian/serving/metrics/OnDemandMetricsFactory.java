package com.yidian.serving.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.google.common.collect.Maps;
import com.yidian.serving.metrics.core.Gauge;
import com.yidian.serving.metrics.core.Histogram;
import com.yidian.serving.metrics.core.Meter;
import com.yidian.serving.metrics.reporter.OpenTsdbReporter;
import com.yidian.serving.metrics.reporter.opentsdb.OpenTsdbClient;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Metrics are created when requested in this OnDemandMetricsFactory.
 * @author jixu
 */
public class OnDemandMetricsFactory implements MetricsFactory {

    private MetricRegistry metricRegistry = new MetricRegistry();
    private final ConcurrentMap<String, Histogram> histogramMap;
    private final ConcurrentMap<String, Meter> meterMap;
    private final ConcurrentMap<String, Gauge> gaugeMap;
    private final OpenTsdbReporter openTsdbReporter;

    public OnDemandMetricsFactory(Map<String, String> tags, OpenTsdbClient openTsdbClient) {
        histogramMap = Maps.newConcurrentMap();
        meterMap = Maps.newConcurrentMap();
        gaugeMap = Maps.newConcurrentMap();

        openTsdbReporter = OpenTsdbReporter.forRegistry(metricRegistry)
                .withTags(tags)
                .build(openTsdbClient);
        openTsdbReporter.start(1, TimeUnit.MINUTES);
    }

    public Histogram getHistogram(String name) {
        if (!histogramMap.containsKey(name)) {
            com.codahale.metrics.Histogram inner =
                    new com.codahale.metrics.Histogram(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES));
            Histogram histogram = new Histogram(name, inner);
            Histogram pre = histogramMap.putIfAbsent(name, histogram);
            if (pre == null) {
                metricRegistry.register(name, inner);
            }
        }
        return histogramMap.get(name);
    }

    public Meter getMeter(String name) {
        if (!meterMap.containsKey(name)) {
            com.codahale.metrics.Meter inner = new com.codahale.metrics.Meter();
            Meter meter = new Meter(name, inner);
            Meter pre = meterMap.putIfAbsent(name, meter);
            if (pre == null) {
                metricRegistry.register(name, inner);
            }
        }
        return meterMap.get(name);
    }

    public <T extends Number> boolean register(final Gauge<T> gauge) {
        if (!gaugeMap.containsKey(gauge.getName())) {
            com.codahale.metrics.Gauge<T> inner = new com.codahale.metrics.Gauge<T>() {
                @Override
                public T getValue() {
                    return gauge.getValue();
                }
            };
            Gauge pre = gaugeMap.putIfAbsent(gauge.getName(), gauge);
            if (pre == null) {
                metricRegistry.register(gauge.getName(), inner);
                return true;
            }
        }
        return false;
    }
}
