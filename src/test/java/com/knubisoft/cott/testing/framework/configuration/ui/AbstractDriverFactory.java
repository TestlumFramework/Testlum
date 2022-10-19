package com.knubisoft.cott.testing.framework.configuration.ui;

import org.openqa.selenium.WebDriver;

public interface AbstractDriverFactory {
    WebDriver createDriver(Object deviceOrBrowser);
}
