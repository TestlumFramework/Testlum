package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Class<?>, ByType> SEARCH_TYPES;

    static {
        final Map<Class<?>, ByType> map = new HashMap<>();
        map.put(Xpath.class, l -> By.xpath(getLocatorsByType(l, Xpath.class)));
        map.put(Id.class, l -> By.id(getLocatorsByType(l, Id.class)));
        map.put(ClassName.class, l -> By.className(getLocatorsByType(l, ClassName.class)));
        map.put(CssSelector.class, l -> By.cssSelector(getLocatorsByType(l, CssSelector.class)));
        map.put(Text.class, l -> By.text(getLocatorsByType(l, Text.class)));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        Set<org.openqa.selenium.By> bySet = new HashSet<>();
        locator.getXpathOrIdOrClassName().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(SEARCH_TYPES.get(clazz).apply(locator));
        });
        return getElementFromLocatorList(bySet, driver);
    }

    public WebElement getElementFromLocatorList(final Set<org.openqa.selenium.By> bySet, final WebDriver driver) {
        boolean anyLocatorSucceeded = false;
        WebElement element = null;
        for (org.openqa.selenium.By by : bySet) {
            try {
                element = driver.findElement(by);
                anyLocatorSucceeded = true;
            } catch (NoSuchElementException ignored) { }
        }
        if (!anyLocatorSucceeded) {
            throw new DefaultFrameworkException("No such element");
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getLocatorsByType(final Locator locator, final Class<T> clazz) {
        Map<Class<?>, List<Object>> locatorTypeMap = splitByLocatorType(locator);
        return (List<T>) locatorTypeMap.entrySet().stream()
                .filter(e -> e.getKey().equals(clazz))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new DefaultFrameworkException("There is no locator type like this"));
    }

    public Map<Class<?>, List<Object>> splitByLocatorType(final Locator locator) {
        List<Object> locators = locator.getXpathOrIdOrClassName();
        return locators.stream()
                .collect(Collectors.groupingBy(Object::getClass));
    }

    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> { }
}
