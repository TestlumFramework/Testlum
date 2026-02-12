package com.knubisoft.comparator;

public enum Mode {
    STRICT,
    LENIENT;

    public void onStrict(final Runnable runnable) {
        if (this == STRICT) {
            runnable.run();
        }
    }
}
