package com.testlum.testing.report;

import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Overview;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ScenarioResult {

    private int id;
    private Overview overview;
    private String name;
    private String tags;
    private String path;
    private String browser;
    private String mobilebrowserDevice;
    private String nativeDevice;

    private boolean success;
    private boolean skipped;
    private String cause;
    private long startedAt;
    private long executionTime;
    private String environment;
    private Map<String, String> variation;

    private List<CommandResult> commands = new ArrayList<>();
}
