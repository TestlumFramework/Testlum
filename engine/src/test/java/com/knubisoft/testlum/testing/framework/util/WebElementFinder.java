package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.autohealing.LocatorAutohealer;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
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
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.File;
import java.time.Duration;

import java.util.Collections;
import java.util.HashMap;
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

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.*;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final String START_HEAL_LOG =
            ANSI_YELLOW
            + "Unable to find element by provided locators. Start healing process..." + ANSI_RESET;

    private static final String HEAL_RESULT_LOG =
            ANSI_GREEN
            + "Element was healed successfully. New values for locator {} were generated in {}" + ANSI_RESET;

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

    public WebElement find(final LocatorData locatorData, final ExecutorDependencies dependencies) {
        Set<org.openqa.selenium.By> bySet = new LinkedHashSet<>();
        locatorData.getLocator().getXpathOrIdOrClassName().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(SEARCH_TYPES.get(clazz).apply(locatorData.getLocator()));
        });
        return getElementFromLocatorList(bySet, dependencies, locatorData);
    }

    private WebElement getElementFromLocatorList(final Set<org.openqa.selenium.By> bySet,
                                                 final ExecutorDependencies dependencies, final LocatorData locatorData) {
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
                logMessages.add(String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE, extractLocatorValue(by)));
            }
        }
        return Optional.empty();
    }

    private static WebElement tryToFindElementIfNotFoundBeforeAfterAutoWait(final Set<org.openqa.selenium.By> bySet,
                                                                            final ExecutorDependencies dependencies,
                                                                            final LocatorData locatorData) {
        waitForSecondsDefinedInConfig();

        Optional<WebElement> optionalElement = findElement(bySet, dependencies.getDriver());

        if (optionalElement.isEmpty()) {
            AutoHealing autoHealing = ConfigProviderImpl.GlobalTestConfigurationProvider
                    .getWebSettings(EnvManager.currentEnv()).getAutoHealing();
            if (autoHealing.isEnabled()) {
                return tryToHealElement(dependencies, locatorData, autoHealing);
            }
            throw new DefaultFrameworkException(
                    String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorData.getLocator().getLocatorId()));
        }
        return optionalElement.get();
    }

    private static WebElement tryToHealElement(final ExecutorDependencies dependencies, final LocatorData locatorData,
                                               final AutoHealing autoHealing) {
        log.warn(START_HEAL_LOG);
        LocatorAutohealer locatorAutohealer = new LocatorAutohealer(dependencies);
        Locator locator = locatorData.getLocator();
        Optional<WebElement> healedWebElement = locatorAutohealer.heal(locator);
        if (healedWebElement.isEmpty()) {
            throw new DefaultFrameworkException(
                    String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locator.getLocatorId()));
        }
        WebElement healedElement = healedWebElement.get();
        File fileWithPatch = locatorAutohealer.generateNewLocators(
                healedElement, autoHealing.getMode(), dependencies, locatorData);
        log.info(HEAL_RESULT_LOG, locator.getLocatorId(),
                fileWithPatch == null ? dependencies.getFile() : fileWithPatch.getAbsolutePath());
        return healedElement;
    }

    //CHECKSTYLE:OFF
    @SneakyThrows(InterruptedException.class)
    private static void waitForSecondsDefinedInConfig() {
        Web settings = ConfigProviderImpl.GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
        int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
        Thread.sleep(secondsToWait * 1000L);
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

    private static WebElement findElementByLocator(final WebDriver driver, final org.openqa.selenium.By by,
                                                   final List<String> logMessages, final int checkedLocatorCount) {
        WebElement element = driver.findElement(by);
        printLogsAboutUndiscoveredElements(logMessages);
        if (checkedLocatorCount != 0) {
            log.info(ELEMENT_WAS_FOUND_BY_LOCATOR, extractLocatorValue(by));
        }
        return element;
    }

    private static void waitForDomToComplete(final ExecutorDependencies dependencies) {
        if (dependencies.getUiType().equals(UiType.NATIVE)) {
            return;
        }
        Web settings = ConfigProviderImpl.GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
        int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
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

    private static void printLogsAboutUndiscoveredElements(final List<String> logMessages) {
        for (String logMessage : logMessages) {
            log.info(logMessage);
        }
    }

    public static String extractLocatorValue(final org.openqa.selenium.By by) {
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
