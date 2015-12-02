package com.yidian.serving.metrics.core;

/**
 * @author jixu
 */
public class Histogram {

    private final String name;
    private final com.codahale.metrics.Histogram inner;

    public Histogram(String name, com.codahale.metrics.Histogram inner) {
        this.name = name;
        this.inner = inner;
    }

    public String getName() {
        return name;
    }

    public void update(long value) {
        inner.update(value);
    }
}
