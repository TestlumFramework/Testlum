package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<LocatorType, ByType> SEARCH_TYPES;

    static {
        final Map<LocatorType, ByType> map = new HashMap<>();
        map.put(l -> Objects.nonNull(l.getXpath()), l -> By.xpath(l.getXpath()));
        map.put(l -> Objects.nonNull(l.getId()), l -> By.id(l.getId()));
        map.put(l -> Objects.nonNull(l.getClazz()), l -> By.className(l.getClazz()));
        map.put(l -> Objects.nonNull(l.getCssSelector()), l -> By.cssSelector(l.getCssSelector()));
        map.put(l -> Objects.nonNull(l.getText()), l -> By.text(l.getText()));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver, final int secondsToWait) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(l -> l.getValue().apply(locator))
                .map(by -> getWebElement(by, driver, secondsToWait))
                .orElseThrow(() -> new DefaultFrameworkException("Web element for locator <%s> not found", locator));
    }

    private WebElement getWebElement(final org.openqa.selenium.By by,
                                     final WebDriver driver,
                                     final int secondsToWait) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(secondsToWait));
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    private interface LocatorType extends Predicate<Locator> { }
    private interface ByType extends Function<Locator, org.openqa.selenium.By> { }
}
