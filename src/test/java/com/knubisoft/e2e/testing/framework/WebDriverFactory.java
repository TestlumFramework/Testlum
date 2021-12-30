package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.constant.SeleniumConstant;
import com.knubisoft.e2e.testing.model.global_config.BrowserSettings;
import com.knubisoft.e2e.testing.model.global_config.Options;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@UtilityClass
public class WebDriverFactory {

    private static final String CHROME = "Chrome";
    private static final String FIREFOX = "Firefox";
    private static final String OPERA = "Opera";
    private static final String EDGE = "Edge";

    private static final Map<String, WebDriverInitializer<? extends WebDriver>> DRIVER_INITIALIZER_MAP;

    static {
        final Map<String, WebDriverInitializer<? extends WebDriver>> map = new HashMap<>(4);

        map.put(CHROME, new ChromeDriverInitializer());
        map.put(FIREFOX, new FirefoxDriverInitializer());
        map.put(OPERA, new OperaDriverInitializer());
        map.put(EDGE, new EdgeDriverInitializer());

        DRIVER_INITIALIZER_MAP = Collections.unmodifiableMap(map);
    }

    public WebDriver create(final String browserVersionElement,
                            final BrowserSettings browserSettings) {
        String[] nameAndVersion = browserVersionElement.split(DelimiterConstant.DOUBLE_UNDERSCORE, 2);
        WebDriverInitializer<? extends WebDriver> driverInitializer = DRIVER_INITIALIZER_MAP.get(nameAndVersion[0]);
        if (SystemInfo.USE_SELENIUM_HUB) {
            return driverInitializer.initRemote(nameAndVersion[1], browserSettings);
        } else {
            return driverInitializer.init(nameAndVersion[1], browserSettings);
        }
    }

    private List<String> toOptions(final BrowserSettings browserSettings) {
        List<String> result = new ArrayList<>();
        Options options = browserSettings.getOptions();
        if (options != null && options.getOption().stream().noneMatch(String::isEmpty)) {
            result.addAll(options.getOption());
        }
        return result;
    }

    private void manageWindowSize(final RemoteWebDriver driver) {
        Dimension dimension = new Dimension(SeleniumConstant.SCREEN_WIDTH, SeleniumConstant.SCREEN_HEIGHT);
        driver.manage().window().setSize(dimension);
        driver.manage().window().maximize();
        driver.get(GlobalTestConfigurationProvider.provide().getUi().getBaseUrl());
    }

    private interface WebDriverInitializer<T extends RemoteWebDriver> {
        T init(final String version, final BrowserSettings browserSettings);

        RemoteWebDriver initRemote(final String version, final BrowserSettings browserSettings);
    }

    private static class ChromeDriverInitializer implements WebDriverInitializer<ChromeDriver> {
        @Override
        public ChromeDriver init(final String version, final BrowserSettings browserSettings) {
            WebDriverManager.chromedriver().browserVersion(version).setup();
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(browserSettings);
            ChromeDriver chromeDriver = new ChromeDriver(cap);
            manageWindowSize(chromeDriver);
            return chromeDriver;
        }

        @SneakyThrows
        @Override
        public RemoteWebDriver initRemote(final String version, final BrowserSettings browserSettings) {
            WebDriverManager.chromedriver().browserVersion(version).setup();
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(browserSettings);
            RemoteWebDriver webDriver = new RemoteWebDriver(new URL(SeleniumConstant.REMOTE_URL), cap);
            manageWindowSize(webDriver);
            return webDriver;
        }

        @NotNull
        private DesiredCapabilities getCapabilitiesAndConfigureDriver(final BrowserSettings browserSettings) {
            final ChromeOptions options = new ChromeOptions();
            final List<String> optionsList = toOptions(browserSettings);
            if (CollectionUtils.isNotEmpty(optionsList)) {
                options.addArguments(optionsList.toArray(new String[0]));
            }
            DesiredCapabilities cap = getDesiredCapabilities(options);
            return cap;
        }

        @NotNull
        private DesiredCapabilities getDesiredCapabilities(final ChromeOptions options) {
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            DesiredCapabilities cap = DesiredCapabilities.chrome();
            cap.setCapability(ChromeOptions.CAPABILITY, options);
            cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            return cap;
        }
    }

    private static class FirefoxDriverInitializer implements WebDriverInitializer<FirefoxDriver> {
        @Override
        public FirefoxDriver init(final String version, final BrowserSettings browserSettings) {
            WebDriverManager.firefoxdriver().browserVersion(version).setup();
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(browserSettings);
            FirefoxDriver firefoxDriver = new FirefoxDriver(cap);
            manageWindowSize(firefoxDriver);
            return firefoxDriver;
        }

        @SneakyThrows
        @Override
        public RemoteWebDriver initRemote(final String version, final BrowserSettings browserSettings) {
            WebDriverManager.firefoxdriver().browserVersion(version).setup();
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(browserSettings);
            RemoteWebDriver webDriver = new RemoteWebDriver(new URL(SeleniumConstant.REMOTE_URL), cap);
            manageWindowSize(webDriver);
            return webDriver;
        }

        @NotNull
        private DesiredCapabilities getCapabilitiesAndConfigureDriver(final BrowserSettings browserSettings) {
            FirefoxOptions options = new FirefoxOptions();
            List<String> optionsList = toOptions(browserSettings);
            if (CollectionUtils.isNotEmpty(optionsList)) {
                options.addArguments(optionsList.toArray(new String[0]));
            }
            DesiredCapabilities cap = getDesiredCapabilities(options);
            return cap;
        }

        @NotNull
        private DesiredCapabilities getDesiredCapabilities(final FirefoxOptions options) {
            DesiredCapabilities cap = DesiredCapabilities.firefox();
            cap.setCapability(FirefoxOptions.FIREFOX_OPTIONS, options);
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
            cap.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
            return cap;
        }
    }

    private static class OperaDriverInitializer implements WebDriverInitializer<FirefoxDriver> {

        @Override
        public FirefoxDriver init(final String version, final BrowserSettings browserSettings) {
            throw new NotImplementedException();
        }

        @Override
        public RemoteWebDriver initRemote(String version, BrowserSettings browserSettings) {
            throw new NotImplementedException();
        }
    }

    private static class EdgeDriverInitializer implements WebDriverInitializer<FirefoxDriver> {

        @Override
        public FirefoxDriver init(final String version, final BrowserSettings browserSettings) {
            throw new NotImplementedException();
        }

        @Override
        public RemoteWebDriver initRemote(final String version, final BrowserSettings browserSettings) {
            throw new NotImplementedException();
        }
    }
}
