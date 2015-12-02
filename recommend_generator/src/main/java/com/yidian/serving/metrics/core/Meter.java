package com.yidian.serving.metrics.core;

/**
 * @author jixu
 */
public class Meter {
    private final String name;
    private final com.codahale.metrics.Meter inner;

    public Meter(String name, com.codahale.metrics.Meter inner) {
        this.name = name;
        this.inner = inner;
    }

    public String getName() {
        return name;
    }

    public void mark() {
        mark(1);
    }

    public void mark(long n) {
        inner.mark(n);
    }
}
