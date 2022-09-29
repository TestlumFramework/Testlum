package com.knubisoft.cott.testing.framework.interpreter.lib;

import lombok.Data;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

@Data
public class Drivers {
    private WebDriver webDriver;
    private WebDriver mobilebrowserDriwer;
    private WebDriver nativeDriver;

    public List<WebDriver> getDriversList() {
        List<WebDriver> drivers = new ArrayList<>();
        drivers.add(getNativeDriver());
        drivers.add(getWebDriver());
        drivers.add(getMobilebrowserDriwer());
        return drivers;
    }
}
