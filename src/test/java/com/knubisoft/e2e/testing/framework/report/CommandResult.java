package com.knubisoft.e2e.testing.framework.report;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;

@Data
public class CommandResult {
    private long id;
    private String commandKey;
    private String comment;

    private String expected;
    private String actual;

    private boolean success;
    private Exception exception;
    private long executionTime;

    private String base64Screenshot;

    private List<CommandResult> subCommandsResult;

    private LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();

    public void put(final Object key, final Object value) {
        this.metadata.put(String.valueOf(key), value);
    }
}
