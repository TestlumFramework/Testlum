package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.autohealing.AutoHealer;
import com.knubisoft.testlum.testing.framework.autohealing.AutoHealerFactory;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.framework.util.check.AbstractElementCheck;
import com.knubisoft.testlum.testing.framework.util.check.ElementCheckChain;
import com.knubisoft.testlum.testing.framework.util.check.PageLoadCheck;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class WebElementFinder {

    private static final int DEFAULT_ELEMENT_AUTO_WAIT_TIME_SECONDS = 30;
    private static final int LOCATOR_PREFIX_LENGTH = 3;
    private static final Pattern LOCATOR_VALUE_PATTERN = Pattern.compile(":\\s*(.*)");

    private final AutoHealerFactory autoHealerFactory;
    private final EnvironmentLoader environmentLoader;
    private final ByService byService;
    private final PageLoadCheck pageLoadCheck;
    private final ElementCheckChain elementCheckChain;

    private Map<Class<?>, ByType> searchByTypes;

    @PostConstruct
    public void init() {
        this.searchByTypes = this.createClassToType();
    }

    private Map<Class<?>, ByType> createClassToType() {
        return Map.of(
                Xpath.class, l -> byService.xpath(getLocatorsByType(l, Xpath.class)),
                Id.class, l -> byService.id(getLocatorsByType(l, Id.class)),
                ClassName.class, l -> byService.className(getLocatorsByType(l, ClassName.class)),
                CssSelector.class, l -> byService.cssSelector(getLocatorsByType(l, CssSelector.class)),
                Text.class, l -> byService.text(getLocatorsByType(l, Text.class)));
    }

    public WebElement find(final LocatorData locatorData, final ExecutorDependencies dependencies,
                           final AbstractElementCheck... checks) {
        this.pageLoadCheck.waitUntilDomReady(dependencies);
        Set<By> bySet = this.constructLocatorSet(locatorData);
        try {
            return this.findByLocators(dependencies, bySet, checks);
        } catch (Exception e) {
            return this.findWithAutoHealing(dependencies, locatorData)
                    .orElseThrow(() -> this.unableToFindElementException(locatorData.getLocator().getLocatorId()));
        }
    }

    private WebElement findByLocators(final ExecutorDependencies executorDependencies,
                                      final Set<org.openqa.selenium.By> bySet,
                                      final AbstractElementCheck... checks) {
        int waitTimeSeconds = this.deriveAutoWaitTime(executorDependencies);
        List<By> byList = new ArrayList<>(bySet);

        return new FluentWait<>(executorDependencies.getDriver())
                .withTimeout(Duration.ofSeconds(waitTimeSeconds))
                .pollingEvery(Duration.ofSeconds(1))
                .until(driver -> tryFindByAny(driver, byList, checks));
    }

    private WebElement tryFindByAny(final WebDriver driver, final List<By> byList,
                                    final AbstractElementCheck... checks) {
        List<String> failedMessages = new ArrayList<>();
        for (int i = 0; i < byList.size(); i++) {
            WebElement element = tryFindElement(driver, byList.get(i), failedMessages, i, checks);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    private WebElement tryFindElement(final WebDriver driver, final By by,
                                      final List<String> failedMessages, final int index,
                                      final AbstractElementCheck... checks) {
        try {
            WebElement element = driver.findElement(by);
            elementCheckChain.verify(driver, element, checks);
            logFindResult(failedMessages, by, index);
            return element;
        } catch (NoSuchElementException e) {
            failedMessages.add(String.format(
                    LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE, extractLocatorValue(by)));
            return null;
        } catch (DefaultFrameworkException e) {
            return null;
        }
    }

    private void logFindResult(final List<String> failedMessages, final By by, final int index) {
        failedMessages.forEach(log::info);
        if (index != 0) {
            log.info(LogMessage.ELEMENT_WAS_FOUND_BY_LOCATOR, extractLocatorValue(by));
        }
    }

    private Optional<WebElement> findWithAutoHealing(
            final ExecutorDependencies dependencies,
            final LocatorData locatorData) {

        return getEnabledAutoHealing()
                .map(autoHealing -> tryToHealElement(dependencies, locatorData, autoHealing));
    }

    private WebElement tryToHealElement(final ExecutorDependencies dependencies, final LocatorData locatorData,
                                        final AutoHealing autoHealing) {
        log.warn(LogMessage.START_HEAL_LOG);
        AutoHealer autoHealer = autoHealerFactory.create(dependencies);
        Locator locator = locatorData.getLocator();
        WebElement healedElement = autoHealer.heal(locator)
                .orElseThrow(() -> new DefaultFrameworkException(
                        String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locator.getLocatorId())));
        logHealResult(autoHealer, healedElement, autoHealing, dependencies, locatorData);
        return healedElement;
    }

    private void logHealResult(final AutoHealer autoHealer, final WebElement healedElement,
                                final AutoHealing autoHealing, final ExecutorDependencies dependencies,
                                final LocatorData locatorData) {
        File fileWithPatch = autoHealer.generateNewLocators(
                healedElement, autoHealing.getMode(), dependencies, locatorData);
        log.info(LogMessage.HEAL_RESULT_LOG, locatorData.getLocator().getLocatorId(),
                fileWithPatch == null ? dependencies.getFile() : fileWithPatch.getAbsolutePath());
    }

    private int deriveAutoWaitTime(final ExecutorDependencies dependencies) {
        return switch (dependencies.getUiType()) {
            case WEB -> this.environmentLoader.getWebSettings(dependencies.getEnvironment())
                    .map(web -> web.getBrowserSettings().getElementAutowait().getSeconds())
                    .orElse(DEFAULT_ELEMENT_AUTO_WAIT_TIME_SECONDS);
            case MOBILE_BROWSER -> this.environmentLoader.getMobileBrowserSettings(dependencies.getEnvironment())
                    .map(mobileBrowser -> mobileBrowser.getElementAutowait().getSeconds())
                    .orElse(DEFAULT_ELEMENT_AUTO_WAIT_TIME_SECONDS);
            case NATIVE -> this.environmentLoader.getNativeSettings(dependencies.getEnvironment())
                    .map(nativeSettings -> nativeSettings.getElementAutowait().getSeconds())
                    .orElse(DEFAULT_ELEMENT_AUTO_WAIT_TIME_SECONDS);
        };
    }

    private Optional<AutoHealing> getEnabledAutoHealing() {
        return environmentLoader.getCurrentEnvWebSettings()
                .map(Web::getAutoHealing)
                .filter(AutoHealing::isEnabled);
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

    public String extractLocatorValue(final org.openqa.selenium.By by) {
        String locator = by.toString().substring(LOCATOR_PREFIX_LENGTH);
        Matcher matcher = LOCATOR_VALUE_PATTERN.matcher(locator);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new DefaultFrameworkException("Unable to extract locator value from: " + by);
    }

    private Set<By> constructLocatorSet(final LocatorData locatorData) {
        Set<By> bySet = new LinkedHashSet<>();
        locatorData.getLocator().getXpathOrIdOrClassName().forEach(obj -> {
            Class<?> clazz = obj.getClass();
            bySet.addAll(searchByTypes.get(clazz).apply(locatorData.getLocator()));
        });
        return bySet;
    }

    private DefaultFrameworkException unableToFindElementException(final String locatorId) {
        return new DefaultFrameworkException(
                String.format(LogMessage.UNABLE_TO_FIND_ELEMENT_BY_LOCATOR, locatorId));
    }

    private interface ByType extends Function<Locator, List<org.openqa.selenium.By>> {
    }
}
