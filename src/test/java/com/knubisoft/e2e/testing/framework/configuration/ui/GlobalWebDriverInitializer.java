package com.knubisoft.e2e.testing.framework.configuration.ui;

import com.knubisoft.e2e.testing.framework.util.BrowserUtil;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class GlobalWebDriverInitializer {

    @Getter
    private List<WebDriver> INITIALIZED_WEB_DRIVERS;

    public void initWebDrivers() {
        INITIALIZED_WEB_DRIVERS = new ArrayList<>();
        BrowserUtil.filterEnabledBrowsers().stream()
                .map(WebDriverFactory::createDriver)
                .forEach(INITIALIZED_WEB_DRIVERS::add);
    }
}
