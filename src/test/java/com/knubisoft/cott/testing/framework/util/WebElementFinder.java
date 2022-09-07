package com.knubisoft.cott.testing.framework.util;

import com.google.common.collect.ImmutableMap;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
        SEARCH_TYPES = ImmutableMap.<Predicate<Locator>, Function<Locator, By>>builder()
                .put(it -> Objects.nonNull(it.getXpath()), it -> By.xpath(it.getXpath()))
                .put(it -> Objects.nonNull(it.getId()), it -> By.id(it.getId()))
                .put(it -> Objects.nonNull(it.getClazz()), it -> By.className(it.getClazz()))
                .put(it -> Objects.nonNull(it.getCssSelector()), it -> By.cssSelector(it.getCssSelector()))
                .put(it -> Objects.nonNull(it.getLinkText()), it -> By.linkText(it.getLinkText()))
                .put(it -> Objects.nonNull(it.getPartialLinkText()), it -> By.partialLinkText(it.getPartialLinkText()))
                .build();
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
