package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ELEMENT_WAS_FOUND_BY_LOCATOR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE;

@Slf4j
@UtilityClass
public final class WebElementFinder {

    private static final Map<Class<?>, ByType> SEARCH_TYPES;
    private static final Pattern ID_VALUE_PATTERN = Pattern.compile("id:\\s*(\\w+)");
    private static final Pattern CSS_SELECTOR_VALUE_PATTERN = Pattern.compile("cssSelector:\\s*(\\S+)");
    private static final Pattern CLASS_NAME_VALUE_PATTERN = Pattern.compile("\\[@class='([^']*)']");
    private static final Pattern TEXT_VALUE_PATTERN = Pattern.compile("contains\\(text\\(\\),\\s*'([^']*)'\\)");
    private static final Pattern XPATH_VALUE_PATTERN = Pattern.compile("xpath:\\s*(\\S+)");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\[@placeholder='([^']*)']");

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
        locator.getElementSelectors().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(SEARCH_TYPES.get(clazz).apply(locator));
        });
        return getElementFromLocatorList(bySet, driver, locator.getLocator());
    }

    public WebElement getElementFromLocatorList(final Set<org.openqa.selenium.By> bySet, final WebDriver driver,
                                                final String locatorId) {
        List<String> logMessages = new ArrayList<>();
        waitForDomToComplete(driver);
        for (org.openqa.selenium.By by : bySet) {
            try {
                WebElement element = driver.findElement(by);
                printLogsAboutUndiscoveredElements(logMessages);
                log.info(ELEMENT_WAS_FOUND_BY_LOCATOR, extractLocatorValue(by));
                return element;
            } catch (NoSuchElementException e) {
                logMessages.add(String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE, extractLocatorValue(by)));
            }
        }
        throw new DefaultFrameworkException(String.format(UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId));
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
        List<Object> locators = locator.getElementSelectors();
        return locators.stream()
                .collect(Collectors.groupingBy(Object::getClass));
    }

    private static void waitForDomToComplete(final WebDriver webDriver) {
        Web settings = ConfigProviderImpl.GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
        int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
        FluentWait<WebDriver> wait = new FluentWait<>(webDriver)
                .withTimeout(Duration.ofSeconds(secondsToWait))
                .pollingEvery(Duration.ofMillis(500L))
                .withMessage("Time out is reached. Page is not loaded!");

        wait.until((ExpectedCondition<Boolean>) driver -> {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String domLoadStatus = (String) Objects.requireNonNull(js)
                    .executeScript("return document.readyState");
            return domLoadStatus.equals("complete");
        });
    }

    private static void printLogsAboutUndiscoveredElements(List<String> logMessages) {
        for (String logMessage : logMessages) {
            log.info(logMessage);
        }
    }

    public static String extractLocatorValue(final org.openqa.selenium.By by) {
        String locator = getLocatorValue(by);
        List<Pattern> locatorValuePatterns =
                List.of(ID_VALUE_PATTERN, CSS_SELECTOR_VALUE_PATTERN, CLASS_NAME_VALUE_PATTERN, TEXT_VALUE_PATTERN, XPATH_VALUE_PATTERN, PLACEHOLDER_PATTERN);

        for (Pattern pattern : locatorValuePatterns) {
            Matcher matcher = pattern.matcher(locator);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        return "No match found";
    }

    @NotNull
    private static String getLocatorValue(final org.openqa.selenium.By by) {
        return by.toString().substring(3);
    }

    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> {
    }
}
