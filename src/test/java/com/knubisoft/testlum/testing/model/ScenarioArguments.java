package com.knubisoft.testlum.testing.model;

import com.knubisoft.testlum.testing.model.scenario.Scenario;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Map;

@Builder
@Getter
@Setter
public class ScenarioArguments {

    private final String path;
    private final File file;
    private final Scenario scenario;
    private final Exception exception;
    private final String browser;
    private final String mobilebrowserDevice;
    private final String nativeDevice;
    private final Map<String, String> variation;
    private final boolean containsUiSteps;

    private String environment;
}
