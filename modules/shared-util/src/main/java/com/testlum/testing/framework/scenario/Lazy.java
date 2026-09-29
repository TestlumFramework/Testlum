package com.testlum.testing.framework.scenario;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {

    private final Supplier<T> supplier;
    private volatile T value;
    private volatile boolean resolved;

    public Lazy(final Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(final Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    @Override
    public T get() {
        if (!resolved) {
            synchronized (this) {
                if (!resolved) {
                    value = supplier.get();
                    resolved = true;
                }
            }
        }
        return value;
    }
}
