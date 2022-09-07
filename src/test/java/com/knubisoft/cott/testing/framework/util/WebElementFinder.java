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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.lang.String.valueOf;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Predicate<Locator>, Function<Locator, By>> chooseSelector =
            ImmutableMap.<Predicate<Locator>, Function<Locator, By>>builder()
                    .put(it -> it.getXpath() != null, it -> By.xpath(it.getXpath()))
                    .put(it -> it.getId() != null, it -> By.id(it.getId()))
                    .put(it -> it.getClazz() != null, it -> By.className(it.getClazz()))
                    .put(it -> it.getCssSelector() != null, it -> By.cssSelector(it.getCssSelector()))
                    .put(it -> it.getLinkText() != null, it -> By.linkText(it.getLinkText()))
                    .put(it -> it.getPartialLinkText() != null, it -> By.partialLinkText(it.getPartialLinkText()))
                    .build();

    public WebElement find(final Locator locator, final WebDriver driver) {
        return chooseSelector.entrySet().stream()
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
