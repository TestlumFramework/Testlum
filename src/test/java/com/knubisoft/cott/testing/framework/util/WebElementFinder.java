package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<LocatorType, ByType> SEARCH_TYPES;

    static {
        final Map<LocatorType, ByType> map = new HashMap<>();
        map.put(l -> nonNull(l.getXpath()), l -> By.xpath(l.getXpath()));
        map.put(l -> nonNull(l.getId()), l -> By.id(l.getId()));
        map.put(l -> nonNull(l.getClazz()), l -> By.className(l.getClazz()));
        map.put(l -> nonNull(l.getCssSelector()), l -> By.cssSelector(l.getCssSelector()));
        map.put(l -> nonNull(l.getText()), l -> By.text(l.getText()));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(l -> l.getValue().apply(locator))
                .map(driver::findElement)
                .orElseThrow(() -> new DefaultFrameworkException("Web element for locator <%s> not found", locator));
    }

    private interface LocatorType extends Predicate<Locator> { }
    private interface ByType extends Function<Locator, org.openqa.selenium.By> { }
}
