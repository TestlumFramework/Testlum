package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.pages.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.*;

@Slf4j
@Component
@RequiredArgsConstructor
public final class WebElementFinder {

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
                logMessages.add(String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE, extractLocatorValue(by)));
            }
        }
        return Optional.empty();
    }

    private WebElement tryToFindElementIfNotFoundBeforeAfterAutoWait(final Set<org.openqa.selenium.By> bySet,
                                                                     final WebDriver driver,
                                                                     final String locatorId) {
        waitForSecondsDefinedInConfig();

        Optional<WebElement> optionalElement = findElement(bySet, driver);

        if (optionalElement.isEmpty()) {
            throw new DefaultFrameworkException(String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId));
        }
        return optionalElement.get();
    }

    //CHECKSTYLE:OFF
    @SneakyThrows(InterruptedException.class)
    private void waitForSecondsDefinedInConfig() {
        Optional<Web> settings = environmentLoader.getCurrentEnvWebSettings();
        if (settings.isPresent()) {
            int secondsToWait = settings.get().getBrowserSettings().getElementAutowait().getSeconds();
            Thread.sleep(secondsToWait * 1000L);
        }
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
            log.info(ELEMENT_WAS_FOUND_BY_LOCATOR, extractLocatorValue(by));
        }
        return element;
    }

    private void waitForDomToComplete(final ExecutorDependencies dependencies) {
        if (dependencies.getUiType().equals(UiType.NATIVE)) {
            return;
        }
        Optional<Web> settings = environmentLoader.getCurrentEnvWebSettings();
        int secondsToWait = settings.get().getBrowserSettings().getElementAutowait().getSeconds();
        FluentWait<WebDriver> wait = new FluentWait<>(dependencies.getDriver())
                .withTimeout(Duration.ofSeconds(secondsToWait))
                .pollingEvery(Duration.ofMillis(500L))
                .withMessage("Time out is reached. Page is not loaded!");

        wait.until((ExpectedCondition<Boolean>) driver -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String domStatus = (String) Objects.requireNonNull(js).executeScript("return document.readyState");
            return domStatus.equals("complete");
        });
    }

    private void printLogsAboutUndiscoveredElements(final List<String> logMessages) {
        for (String logMessage : logMessages) {
            log.info(logMessage);
        }
    }

    public String extractLocatorValue(final org.openqa.selenium.By by) {
        String locator = by.toString().substring(3);
        Pattern pattern = Pattern.compile(":\\s*(.*)");
        Matcher matcher = pattern.matcher(locator);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return "No match found";
    }

    //CHECKSTYLE:ON

    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> {
    }
}
