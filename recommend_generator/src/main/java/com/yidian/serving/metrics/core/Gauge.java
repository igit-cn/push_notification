package com.yidian.serving.metrics.core;

/**
 * @author jixu
 */
public abstract class Gauge<T extends Number> {
    private final String name;

    public Gauge(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract T getValue();
}
