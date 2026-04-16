package com.knubisoft.testlum.testing.framework.util;

import org.openqa.selenium.WebDriver;

public interface JsScriptExecutor {

    Object executeJsScript(String script, WebDriver driver, Object... args);
}
