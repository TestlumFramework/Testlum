package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    public WebElement find(final Locator locator, final WebDriver driver) {
        if (locator.getXpath() != null) {
            return driver.findElement(By.xpath(locator.getXpath()));
        } else if (locator.getId() != null) {
            return driver.findElement(By.id(locator.getId()));
        } else if (locator.getClazz() != null) {
            return driver.findElement(By.className(locator.getClazz()));
        }
        throw defaultFrameworkException(locator);
    }

    private DefaultFrameworkException defaultFrameworkException(final Locator locator) {
        log.error("Web element for locator='{}' not found", locator);
        return new DefaultFrameworkException(format("Web element for locator='%s' not found", locator));
    }
}
