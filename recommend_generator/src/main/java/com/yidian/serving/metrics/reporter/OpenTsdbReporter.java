package com.yidian.serving.metrics.reporter;

import com.codahale.metrics.*;
import com.google.common.collect.Lists;
import com.yidian.serving.metrics.reporter.opentsdb.OpenTsdbClient;
import com.yidian.serving.metrics.reporter.opentsdb.OpenTsdbMetric;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reporter who push metrics values to an OpenTSDB server.
 * @author jixu
 */
public class OpenTsdbReporter extends ScheduledReporter {

    private final OpenTsdbClient openTsdbClient;
    private final Clock clock;
    private final Map<String, String> tags;

    /**
     * Returns a new {@link Builder} for {@link OpenTsdbReporter}.
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link OpenTsdbReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link OpenTsdbReporter} instances. Default to use default
     * clock and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private MetricFilter filter;
        private Map<String, String> tags;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
        }

        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder withTags(Map<String, String> tags) {
            this.tags = tags;
            return this;
        }

        public OpenTsdbReporter build(OpenTsdbClient client) {
            return new OpenTsdbReporter(registry, client, clock, filter, tags);
        }
    }

    private OpenTsdbReporter(MetricRegistry registry, OpenTsdbClient openTsdbClient,
                             Clock clock, MetricFilter filter, Map<String, String> tags) {
        super(registry, "opentsdb-reporter", filter, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
        this.openTsdbClient = openTsdbClient;
        this.clock = clock;
        this.tags = tags;
    }

    /**
     * Only support histograms and meters. For histograms, only mean, p95 and p99 are reported.
     * For meters, only m1 is reported.
     */
    @Override
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        long rawTs = clock.getTime() / 1000;
        long timestamp = rawTs - rawTs % 60;
        List<OpenTsdbMetric> metrics = Lists.newArrayList();

        for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
            String name = entry.getKey();
            Snapshot snapshot = entry.getValue().getSnapshot();
            metrics.add(new OpenTsdbMetric(name + ".mean", timestamp, snapshot.getMean(), tags));
            metrics.add(new OpenTsdbMetric(name + ".p95", timestamp, snapshot.get95thPercentile(), tags));
            metrics.add(new OpenTsdbMetric(name + ".p99", timestamp, snapshot.get99thPercentile(), tags));
        }

        for (Map.Entry<String, Meter> entry : meters.entrySet()) {
            String name = entry.getKey();
            metrics.add(new OpenTsdbMetric(name + ".m1", timestamp, entry.getValue().getOneMinuteRate(), tags));
        }

        for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            String name = entry.getKey();
            metrics.add(new OpenTsdbMetric(name, timestamp, (Number) entry.getValue().getValue(), tags));
        }

        openTsdbClient.send(metrics);
    }
}
