package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.BrowserInDocker;
import com.knubisoft.testlum.testing.model.global_config.BrowserOptionsArguments;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackWeb;
import com.knubisoft.testlum.testing.model.global_config.Capabilities;
import com.knubisoft.testlum.testing.model.global_config.Chrome;
import com.knubisoft.testlum.testing.model.global_config.Edge;
import com.knubisoft.testlum.testing.model.global_config.Firefox;
import com.knubisoft.testlum.testing.model.global_config.LocalBrowser;
import com.knubisoft.testlum.testing.model.global_config.Opera;
import com.knubisoft.testlum.testing.model.global_config.RemoteBrowser;
import com.knubisoft.testlum.testing.model.global_config.Safari;
import com.knubisoft.testlum.testing.model.global_config.ScreenRecording;
import com.knubisoft.testlum.testing.model.global_config.Web;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.OperaDriverManager;
import io.github.bonigarcia.wdm.managers.SafariDriverManager;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DRIVER_INITIALIZER_NOT_FOUND;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class WebDriverFactory {
    private static final String DEFAULT_DOCKER_SCREEN_COLORS_DEPTH = "x24";
    private static final Map<BrowserPredicate, WebDriverFunction> DRIVER_INITIALIZER_MAP;

    static {
        final Map<BrowserPredicate, WebDriverFunction> map = new HashMap<>(5);
        map.put(browser -> browser instanceof Chrome, b -> new ChromeDriverInitializer().init((Chrome) b));
        map.put(browser -> browser instanceof Firefox, b -> new FirefoxDriverInitializer().init((Firefox) b));
        map.put(browser -> browser instanceof Opera, b -> new OperaDriverInitializer().init((Opera) b));
        map.put(browser -> browser instanceof Safari, b -> new SafariDriverInitializer().init((Safari) b));
        map.put(browser -> browser instanceof Edge, b -> new EdgeDriverInitializer().init((Edge) b));
        DRIVER_INITIALIZER_MAP = Collections.unmodifiableMap(map);
    }

    public WebDriver createDriver(final AbstractBrowser browser) {
        WebDriver webDriver = DRIVER_INITIALIZER_MAP.keySet().stream()
                .filter(key -> key.test(browser))
                .map(DRIVER_INITIALIZER_MAP::get)
                .map(webDriverFunction -> webDriverFunction.apply(browser))
                .peek(driver -> BrowserUtil.manageWindowSize(browser, driver))
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(DRIVER_INITIALIZER_NOT_FOUND));
        Web settings = GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
        int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        webDriver.get(settings.getBaseUrl());
        return webDriver;
    }

    private WebDriver getWebDriver(final AbstractBrowser browser,
                                   final MutableCapabilities browserOptions,
                                   final WebDriverManager driverManager) {
        setCapabilities(browser, browserOptions);
        switch (BrowserUtil.getBrowserType(browser)) {
            case BROWSER_STACK:
                return getBrowserStackDriver(browser.getBrowserType().getBrowserStack(), browserOptions);
            case REMOTE:
                return getRemoteDriver(browser.getBrowserType().getRemoteBrowser(), browserOptions);
            case IN_DOCKER:
                WebDriverManager webDriverManager = setScreenResolution(browser, driverManager);
                return getBrowserInDocker(browser.getBrowserType().getBrowserInDocker(),
                        browserOptions, webDriverManager);
            default:
                return getLocalDriver(browser.getBrowserType().getLocalBrowser(), browserOptions, driverManager);
        }
    }

    @SneakyThrows
    private WebDriver getBrowserStackDriver(final BrowserStackWeb browserStack,
                                            final MutableCapabilities browserOptions) {
        browserOptions.setCapability("browserstack.local", Boolean.TRUE);
        browserOptions.setCapability(CapabilityType.BROWSER_VERSION, browserStack.getBrowserVersion());
        String browserStackUrl = SeleniumDriverUtil.getBrowserStackUrl(
                GlobalTestConfigurationProvider.getUiConfigs().get(EnvManager.currentEnv()));
        return new RemoteWebDriver(new URL(browserStackUrl), browserOptions);
    }

    @SneakyThrows
    private WebDriver getRemoteDriver(final RemoteBrowser remoteBrowserSettings,
                                      final MutableCapabilities browserOptions) {
        browserOptions.setCapability(CapabilityType.BROWSER_VERSION, remoteBrowserSettings.getBrowserVersion());
        return new RemoteWebDriver(new URL(remoteBrowserSettings.getRemoteBrowserURL()), browserOptions);
    }

    private WebDriverManager setScreenResolution(final AbstractBrowser browser,
                                                 final WebDriverManager driverManager) {
        return isNotBlank(browser.getBrowserWindowSize())
                ? driverManager.browserInDocker().dockerScreenResolution(browser.getBrowserWindowSize()
                + DEFAULT_DOCKER_SCREEN_COLORS_DEPTH) : driverManager.browserInDocker();
    }

    private WebDriver getBrowserInDocker(final BrowserInDocker browserInDockerSettings,
                                         final MutableCapabilities browserOptions,
                                         final WebDriverManager driverManager) {
        String dockerNetwork = browserInDockerSettings.getDockerNetwork();
        ScreenRecording screenRecordingSettings = browserInDockerSettings.getScreenRecording();
        driverManager.capabilities(browserOptions).browserVersion(browserInDockerSettings.getBrowserVersion());
        if (isNotBlank(dockerNetwork)) {
            driverManager.dockerNetwork(dockerNetwork);
        }
        if (nonNull(screenRecordingSettings) && screenRecordingSettings.isEnabled()) {
            driverManager.enableRecording().dockerRecordingOutput(screenRecordingSettings.getOutputFolder());
        }
        return browserInDockerSettings.isEnableVNC() ? driverManager.enableVnc().create() : driverManager.create();
    }

    private WebDriver getLocalDriver(final LocalBrowser localBrowserSettings,
                                     final MutableCapabilities browserOptions,
                                     final WebDriverManager driverManager) {
        String driverVersion = localBrowserSettings.getDriverVersion();
        if (isNotBlank(driverVersion)) {
            driverManager.driverVersion(driverVersion);
        }
        return driverManager.capabilities(browserOptions).create();
    }

    private void setCapabilities(final AbstractBrowser browser, final MutableCapabilities driverOptions) {
        Capabilities capabilities = browser.getCapabilities();
        if (nonNull(capabilities)) {
            capabilities.getCapability().forEach(cap -> driverOptions.setCapability(cap.getName(), cap.getValue()));
        }
    }

    private interface WebDriverInitializer<T extends AbstractBrowser> {
        WebDriver init(T browser);
    }

    private class ChromeDriverInitializer implements WebDriverInitializer<Chrome> {

        @Override
        public WebDriver init(final Chrome browser) {
            return getWebDriver(browser, getChromeOptions(browser), new ChromeDriverManager());
        }

        private ChromeOptions getChromeOptions(final Chrome browser) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setHeadless(browser.isHeadlessMode());
            BrowserOptionsArguments browserOptionsArguments = browser.getChromeOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                chromeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return chromeOptions;
        }
    }

    private class FirefoxDriverInitializer implements WebDriverInitializer<Firefox> {

        @Override
        public WebDriver init(final Firefox browser) {
            return getWebDriver(browser, getFirefoxOptions(browser), new FirefoxDriverManager());
        }

        private FirefoxOptions getFirefoxOptions(final Firefox browser) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setHeadless(browser.isHeadlessMode());
            BrowserOptionsArguments browserOptionsArguments = browser.getFirefoxOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                firefoxOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return firefoxOptions;
        }
    }

    private class EdgeDriverInitializer implements WebDriverInitializer<Edge> {

        @Override
        public WebDriver init(final Edge browser) {
            return getWebDriver(browser, getEdgeOptions(browser), new EdgeDriverManager());
        }

        private EdgeOptions getEdgeOptions(final Edge browser) {
            EdgeOptions edgeOptions = new EdgeOptions();
            edgeOptions.setHeadless(browser.isHeadlessMode());
            BrowserOptionsArguments browserOptionsArguments = browser.getEdgeOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                edgeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return edgeOptions;
        }
    }

    private class SafariDriverInitializer implements WebDriverInitializer<Safari> {

        @Override
        public WebDriver init(final Safari browser) {
            return getWebDriver(browser, new SafariOptions(), new SafariDriverManager());
        }
    }

    private class OperaDriverInitializer implements WebDriverInitializer<Opera> {

        @Override
        public WebDriver init(final Opera browser) {
            return getWebDriver(browser, getOperaOptions(browser), new OperaDriverManager());
        }

        private ChromeOptions getOperaOptions(final Opera browser) {
            ChromeOptions operaOptions = new ChromeOptions();
            BrowserOptionsArguments browserOptionsArguments = browser.getOperaOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                operaOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return operaOptions;
        }
    }

    private interface BrowserPredicate extends Predicate<AbstractBrowser> { }
    private interface WebDriverFunction extends Function<AbstractBrowser, WebDriver> { }
}
