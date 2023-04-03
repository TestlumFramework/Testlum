package com.knubisoft.cott.testing.framework.report;

import com.knubisoft.cott.testing.model.scenario.Overview;
import com.knubisoft.cott.testing.model.scenario.Tags;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScenarioResult {

    private int id;
    private Overview overview;
    private String name;
    private Tags tags;
    private String path;
    private String browser;
    private String mobilebrowserDevice;
    private String nativeDevice;

    private boolean success;
    private String cause;
    private long executionTime;
    private String environment;

    private List<CommandResult> commands = new ArrayList<>();
}
