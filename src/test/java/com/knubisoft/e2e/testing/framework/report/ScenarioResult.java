package com.knubisoft.e2e.testing.framework.report;

import com.knubisoft.e2e.testing.model.scenario.Overview;
import com.knubisoft.e2e.testing.model.scenario.Tags;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScenarioResult {

    private int id;
    private Overview overview;
    private Tags tags;
    private String path;
    private String name;

    private boolean success;
    private String cause;
    private long executionTime;

    private List<CommandResult> commands = new ArrayList<>();
}
