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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FOUND_MORE_THEN_ONE_XPATH;
import static java.lang.String.format;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Predicate<Locator>, BiFunction<Locator, WebDriver, By>> SEARCH_TYPES;
    private static String FIND_XPATH_BY_TEXT;

    static {
        final Map<Predicate<Locator>, BiFunction<Locator, WebDriver, By>> map = new HashMap<>();
        map.put(l -> Objects.nonNull(l.getXpath()), (l, d) -> By.xpath(l.getXpath()));
        map.put(l -> Objects.nonNull(l.getId()), (l, d) -> By.id(l.getId()));
        map.put(l -> Objects.nonNull(l.getClazz()), (l, d) -> By.className(l.getClazz()));
        map.put(l -> Objects.nonNull(l.getCssSelector()), (l, d) -> By.cssSelector(l.getCssSelector()));
        map.put(l -> Objects.nonNull(l.getText()),
                (l, d) -> By.xpath(getXpathByText(l.getText(), d)));
        SEARCH_TYPES = Collections.unmodifiableMap(map);

    FIND_XPATH_BY_TEXT = FileSearcher.searchFileToString("findXpathByText.js",
            new File("src/test/resources/findXpathByText.js"));
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(l -> l.getValue().apply(locator, driver))
                .map(driver::findElement)
                .orElseThrow(() -> defaultFrameworkException(locator));
    }

    private String getXpathByText(final String text, final WebDriver driver) {
        String script = String.format(FIND_XPATH_BY_TEXT, text);
        List<String> xpathsByText = Collections
                .singletonList(JavascriptUtil.executeJsScriptAndReturnString(script, driver));
        if (xpathsByText.size() > 1) {
            throw new DefaultFrameworkException(FOUND_MORE_THEN_ONE_XPATH, text);
        }
        return xpathsByText.get(0);
    }

    private DefaultFrameworkException defaultFrameworkException(final Locator locator) {
        log.error("Web element for locator='{}' not found", locator);
        return new DefaultFrameworkException(format("Web element for locator='%s' not found", locator));
    }
}
