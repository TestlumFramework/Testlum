package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Predicate<Locator>, Function<Locator, By>> SEARCH_TYPES;

    static {
        final Map<Predicate<Locator>, Function<Locator, By>> map = new HashMap<>();
        map.put(locator -> Objects.nonNull(locator.getXpath()), locator -> By.xpath(locator.getXpath()));
        map.put(locator -> Objects.nonNull(locator.getId()), locator -> By.id(locator.getId()));
        map.put(locator -> Objects.nonNull(locator.getClazz()), locator -> By.className(locator.getClazz()));
        map.put(locator -> Objects.nonNull(locator.getCssSelector()), locator -> By.cssSelector(locator.getCssSelector()));
        map.put(locator -> Objects.nonNull(locator.getLinkText()), locator -> By.linkText(locator.getLinkText()));
        map.put(locator -> Objects.nonNull(locator.getPartialLinkText()), locator -> By.partialLinkText(locator.getPartialLinkText()));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(it -> it.getKey().test(locator))
                .findFirst()
                .map(it -> it.getValue().apply(locator))
                .map(driver::findElement)
                .orElseThrow(() -> defaultFrameworkException(locator));
    }

    private DefaultFrameworkException defaultFrameworkException(final Locator locator) {
        log.error("Web element for locator='{}' not found", locator);
        return new DefaultFrameworkException(format("Web element for locator='%s' not found", locator));
    }
}
