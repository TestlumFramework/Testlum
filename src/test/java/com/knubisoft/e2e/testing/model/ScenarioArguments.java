package com.knubisoft.e2e.testing.model;

import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.scenario.Scenario;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Map;

@RequiredArgsConstructor
@Builder
@Getter
public class ScenarioArguments {

    private final String path;
    private final File file;
    private final Scenario scenario;
    private final Exception exception;
    private final AbstractBrowser browser;
    private final BrowserSettings browserSettings;
    private final Map<String, String> variation;
    private final boolean containsUiSteps;
}
