package com.yidian.serving.metrics;

/**
 * A MetricsFactory Getter. If no MetricsFactory is registered, a PlaceholderMetricsFactory will be used.
 * register method should be called when server starts.
 * @author jixu
 */
public class MetricsFactoryUtil {
    private static volatile MetricsFactory metricsFactory = new PlaceholderMetricsFactory();

    /**
     * This method should be called only once.
     */
    public static void register(MetricsFactory factory) {
        metricsFactory = factory;
    }

    public static MetricsFactory getRegisteredFactory() {
        return metricsFactory;
    }
}
