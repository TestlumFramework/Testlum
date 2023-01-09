package com.knubisoft.cott.testing.model;

import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.scenario.Scenario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
public class ScenarioArguments {

    private String path;
    private File file;
    private Scenario scenario;
    private Exception exception;
    private AbstractBrowser browser;
    private NativeDevice nativeDevice;
    private MobilebrowserDevice mobilebrowserDevice;
    private Map<String, String> variation;
    private boolean containsUiSteps;

    public void setBrowser(AbstractBrowser browser) {
        this.browser = browser;
    }
}
