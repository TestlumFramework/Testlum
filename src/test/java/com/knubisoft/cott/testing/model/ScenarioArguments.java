package com.knubisoft.cott.testing.model;

import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.BrowserStack;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.scenario.Scenario;
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
    private final NativeDevice nativeDevice;
    private final MobilebrowserDevice mobilebrowserDevice;
    private final BrowserStack browserStack;
    private final Map<String, String> variation;
    private final boolean containsUiSteps;
}
