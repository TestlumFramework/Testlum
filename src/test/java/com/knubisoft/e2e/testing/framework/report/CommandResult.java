package com.knubisoft.e2e.testing.framework.report;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Data
public class CommandResult {
    private long id;
    private String commandKey;
    private String comment;

    private String expected;
    private String actual;

    private boolean success;
    private String cause;
    private long executionTime;

    private LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();

    public void put(final Object key, final Object value) {
        this.metadata.put(String.valueOf(key).toLowerCase(Locale.ROOT), value);
    }
}
