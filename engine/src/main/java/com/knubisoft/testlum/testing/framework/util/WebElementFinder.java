package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.autohealing.LocatorAutohealer;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.model.global_config.AutoHealing;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.io.File;
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

    public WebElement find(final LocatorData locatorData, final ExecutorDependencies dependencies) {
        Set<org.openqa.selenium.By> bySet = new LinkedHashSet<>();
        locatorData.getLocator().getXpathOrIdOrClassName().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(searchByTypes.get(clazz).apply(locatorData.getLocator()));
        });
        return getElementFromLocatorList(bySet, dependencies, locatorData);
    }

    private WebElement getElementFromLocatorList(final Set<org.openqa.selenium.By> bySet,
                                                 final ExecutorDependencies dependencies,
                                                 final LocatorData locatorData) {
        waitForDomToComplete(dependencies);

        Optional<WebElement> optionalElement = findElement(bySet, dependencies.getDriver());

        return optionalElement.orElseGet(() ->
                tryToFindElementIfNotFoundBeforeAfterAutoWait(bySet, dependencies, locatorData));
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

    private WebElement tryToFindElementIfNotFoundBeforeAfterAutoWait(
            final Set<By> bySet,
            final ExecutorDependencies dependencies,
            final LocatorData locatorData) {

        String locatorId = locatorData.getLocator().getLocatorId();
        WebElement element = findElementWithAutoWait(bySet, dependencies, locatorId);

        if (element != null) {
            return element;
        }

        return findWithAutoHealing(dependencies, locatorData)
                .orElseThrow(() -> unableToFindElementException(locatorId));
    }

    private Optional<WebElement> findWithAutoHealing(
            final ExecutorDependencies dependencies,
            final LocatorData locatorData) {

        return getEnabledAutoHealing()
                .map(autoHealing -> tryToHealElement(dependencies, locatorData, autoHealing));
    }

    private Optional<AutoHealing> getEnabledAutoHealing() {
        return environmentLoader.getCurrentEnvWebSettings()
                .map(Web::getAutoHealing)
                .filter(AutoHealing::isEnabled);
    }

    private WebElement findElementWithAutoWait(
            final Set<By> bySet,
            final ExecutorDependencies dependencies,
            final String locatorId) {

        int seconds = getAutowaitSeconds(locatorId);
        FluentWait<WebDriver> wait = buildFluentWait(dependencies.getDriver(), seconds)
                .ignoring(NoSuchElementException.class);

        try {
            return wait.until(driver -> findElement(bySet, driver).orElse(null));
        } catch (TimeoutException e) {
            return null;
        } catch (Exception e) {
            throw unableToFindElementException(locatorId);
        }
    }

    private DefaultFrameworkException unableToFindElementException(final String locatorId) {
        return new DefaultFrameworkException(
                String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId));
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

    //CHECKSTYLE:OFF
    private WebElement tryToHealElement(final ExecutorDependencies dependencies, final LocatorData locatorData,
                                        final AutoHealing autoHealing) {
        log.warn(LogMessage.START_HEAL_LOG);
        LocatorAutohealer locatorAutohealer = new LocatorAutohealer(dependencies);
        Locator locator = locatorData.getLocator();
        Optional<WebElement> healedWebElement = locatorAutohealer.heal(locator);
        if (healedWebElement.isEmpty()) {
            throw new DefaultFrameworkException(
                    String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locator.getLocatorId()));
        }
        WebElement healedElement = healedWebElement.get();
        File fileWithPatch = locatorAutohealer.generateNewLocators(
                healedElement, autoHealing.getMode(), dependencies, locatorData);
        log.info(LogMessage.HEAL_RESULT_LOG,
                locator.getLocatorId(), fileWithPatch == null ? dependencies.getFile()
                        : fileWithPatch.getAbsolutePath());
        return healedElement;
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
