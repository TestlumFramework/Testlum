package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Predicate<Locator>, Function<Locator, By>> SEARCH_TYPES;


    static {
        final Map<Predicate<Locator>, Function<Locator, By>> map = new HashMap<>();
        map.put(l -> Objects.nonNull(l.getXpath()), l -> By.xpath(l.getXpath()));
        map.put(l -> Objects.nonNull(l.getId()), l -> By.id(l.getId()));
        map.put(l -> Objects.nonNull(l.getClazz()), l -> By.className(l.getClazz()));
        map.put(l -> Objects.nonNull(l.getCssSelector()), l -> By.cssSelector(l.getCssSelector()));
        map.put(l -> Objects.nonNull(l.getText()), l -> ByText.text(l.getText()));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(l -> l.getValue().apply(locator))
                .map(driver::findElement)
                .orElseThrow(() -> defaultFrameworkException(locator));
    }

    private DefaultFrameworkException defaultFrameworkException(final Locator locator) {
        log.error("Web element for locator='{}' not found", locator);
        return new DefaultFrameworkException(format("Web element for locator='%s' not found", locator));
    }
}
