package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class WebElementFinder {

    private static final long POLLING_INTERVAL_MS = 500L;
    private static final int LOCATOR_PREFIX_LENGTH = 3;
    private static final Pattern LOCATOR_VALUE_PATTERN = Pattern.compile(":\\s*(.*)");
    private static final String DOM_COMPLETE = "complete";

    private final EnvironmentLoader environmentLoader;
    private final ByService byService;

    private Map<Class<?>, ByType> searchByTypes;

    @PostConstruct
    public void init() {
        this.searchByTypes = createClassToType();
    }

    private Map<Class<?>, ByType> createClassToType() {
        return Map.of(
                Xpath.class, l -> byService.xpath(getLocatorsByType(l, Xpath.class)),
                Id.class, l -> byService.id(getLocatorsByType(l, Id.class)),
                ClassName.class, l -> byService.className(getLocatorsByType(l, ClassName.class)),
                CssSelector.class, l -> byService.cssSelector(getLocatorsByType(l, CssSelector.class)),
                Text.class, l -> byService.text(getLocatorsByType(l, Text.class)));
    }

    public WebElement find(final Locator locator, final ExecutorDependencies dependencies) {
        Set<org.openqa.selenium.By> bySet = new LinkedHashSet<>();
        locator.getXpathOrIdOrClassName().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(searchByTypes.get(clazz).apply(locator));
        });
        return getElementFromLocatorList(bySet, dependencies, locator.getLocatorId());
    }

    private WebElement getElementFromLocatorList(final Set<org.openqa.selenium.By> bySet,
                                                 final ExecutorDependencies dependencies, final String locatorId) {
        waitForDomToComplete(dependencies);

        Optional<WebElement> optionalElement = findElement(bySet, dependencies.getDriver());

        return optionalElement.orElseGet(() ->
                tryToFindElementIfNotFoundBeforeAfterAutoWait(bySet, dependencies.getDriver(), locatorId));
    }

    private Optional<WebElement> findElement(final Set<org.openqa.selenium.By> bySet, final WebDriver driver) {
        List<String> logMessages = new LinkedList<>();
        int checkedLocatorCount = 0;
        for (org.openqa.selenium.By by : bySet) {
            try {
                return Optional.of(findElementByLocator(driver, by, logMessages, checkedLocatorCount));
            } catch (NoSuchElementException e) {
                checkedLocatorCount++;
                logMessages.add(String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE,
                        extractLocatorValue(by)));
            }
        }
        return Optional.empty();
    }

    private WebElement tryToFindElementIfNotFoundBeforeAfterAutoWait(final Set<org.openqa.selenium.By> bySet,
                                                                     final WebDriver driver,
                                                                     final String locatorId) {
        int seconds = getAutowaitSeconds(locatorId);
        FluentWait<WebDriver> wait = buildFluentWait(driver, seconds)
                .ignoring(NoSuchElementException.class);
        try {
            return wait.until(d -> findElement(bySet, d).orElse(null));
        } catch (Exception e) {
            throw new DefaultFrameworkException(String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId));
        }
    }

    private int getAutowaitSeconds(final String locatorId) {
        return environmentLoader.getCurrentEnvWebSettings()
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId)))
                .getBrowserSettings().getElementAutowait().getSeconds();
    }

    private FluentWait<WebDriver> buildFluentWait(final WebDriver driver, final int seconds) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(seconds))
                .pollingEvery(Duration.ofMillis(POLLING_INTERVAL_MS));
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

    private WebElement findElementByLocator(final WebDriver driver, final org.openqa.selenium.By by,
                                            final List<String> logMessages, final int checkedLocatorCount) {
        WebElement element = driver.findElement(by);
        printLogsAboutUndiscoveredElements(logMessages);
        if (checkedLocatorCount != 0) {
            log.info(LogMessage.ELEMENT_WAS_FOUND_BY_LOCATOR, extractLocatorValue(by));
        }
        return element;
    }

    private void waitForDomToComplete(final ExecutorDependencies dependencies) {
        if (dependencies.getUiType().equals(UiType.NATIVE)) {
            return;
        }
        int seconds = environmentLoader.getCurrentEnvWebSettings()
                .orElseThrow(() -> new DefaultFrameworkException("Web settings are not configured"))
                .getBrowserSettings().getElementAutowait().getSeconds();
        buildFluentWait(dependencies.getDriver(), seconds)
                .withMessage("Time out is reached. Page is not loaded!")
                .until((ExpectedCondition<Boolean>) this::isDomComplete);
    }

    private boolean isDomComplete(final WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String domStatus = (String) Objects.requireNonNull(js).executeScript("return document.readyState");
        return DOM_COMPLETE.equals(domStatus);
    }

    private void printLogsAboutUndiscoveredElements(final List<String> logMessages) {
        for (String logMessage : logMessages) {
            log.info(logMessage);
        }
    }

    public String extractLocatorValue(final org.openqa.selenium.By by) {
        String locator = by.toString().substring(LOCATOR_PREFIX_LENGTH);
        Matcher matcher = LOCATOR_VALUE_PATTERN.matcher(locator);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new DefaultFrameworkException("Unable to extract locator value from: " + by);
    }

    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> {
    }
}
