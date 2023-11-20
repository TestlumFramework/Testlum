package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<LocatorType, ByType> SEARCH_TYPES;

    static {
        final Map<LocatorType, ByType> map = new HashMap<>();
        map.put(l -> nonNull(getLocatorsByType(l, Xpath.class)), l -> By.xpath(getLocatorsByType(l, Xpath.class)));
        map.put(l -> nonNull(getLocatorsByType(l, Id.class)), l -> By.id(getLocatorsByType(l, Id.class)));
        map.put(l -> nonNull(getLocatorsByType(l, ClassName.class)), l -> By.className(getLocatorsByType(l, ClassName.class)));
        map.put(l -> nonNull(getLocatorsByType(l, CssSelector.class)), l -> By.cssSelector(getLocatorsByType(l, CssSelector.class)));
        map.put(l -> nonNull(getLocatorsByType(l, Text.class)), l -> By.text(getLocatorsByType(l, Text.class)));
        SEARCH_TYPES = Collections.unmodifiableMap(map);
    }

    public WebElement find(final Locator locator, final WebDriver driver) {
        return SEARCH_TYPES.entrySet().stream()
                .filter(l -> l.getKey().test(locator))
                .findFirst()
                .map(l -> l.getValue().apply(locator))
                .map(byList -> getElementFromLocatorList(byList, driver))
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.WEB_ELEMENT_BY_LOCATOR_NOT_FOUND,
                        locator.getLocatorId()));
    }

    public WebElement getElementFromLocatorList(final List<org.openqa.selenium.By> byList, final WebDriver driver) {
        boolean anyLocatorSucceeded = false;
        WebElement element = null;
        for (org.openqa.selenium.By by : byList) {
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
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException("There is no locator type like this"));
    }

    public Map<Class<?>, List<Object>> splitByLocatorType(final Locator locator) {
        List<Object> locators = locator.getXpathOrIdOrClassName();
        return locators.stream()
                .collect(Collectors.groupingBy(Object::getClass));
    }


    private interface LocatorType extends Predicate<Locator> { }
    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> { }
}
