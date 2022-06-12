package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.util.BrowserUtil;
import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.BrowserOptionsArguments;
import com.knubisoft.e2e.testing.model.global_config.BrowserVersion;
import com.knubisoft.e2e.testing.model.global_config.Capabilities;
import com.knubisoft.e2e.testing.model.global_config.Chrome;
import com.knubisoft.e2e.testing.model.global_config.Edge;
import com.knubisoft.e2e.testing.model.global_config.Firefox;
import com.knubisoft.e2e.testing.model.global_config.LocallyRunModeType;
import com.knubisoft.e2e.testing.model.global_config.Opera;
import com.knubisoft.e2e.testing.model.global_config.RunMode;
import com.knubisoft.e2e.testing.model.global_config.Safari;
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
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.DRIVER_INITIALIZER_NOT_FOUND;
import static org.openqa.selenium.remote.CapabilityType.BROWSER_VERSION;

@UtilityClass
public class WebDriverFactory {

    private static final String LATEST = "latest";
    private static final String URL_FOR_TESTING = GlobalTestConfigurationProvider.provide().getUi().getBaseUrl();
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
                .peek(driver -> {
                    if (browser.isMaximizedBrowserWindow()) {
                        driver.manage().window().maximize();
                    }
                })
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(DRIVER_INITIALIZER_NOT_FOUND));
        webDriver.get(URL_FOR_TESTING);
        return webDriver;
    }

    private WebDriver getWebDriver(final AbstractBrowser browser,
                                  final MutableCapabilities browserOptions,
                                  final WebDriverManager webDriverManager) {
        RunMode runMode = browser.getRunMode();
        if (runMode.getLocallyRunMode() != null) {
            return getDriverLocally(browser, webDriverManager, browserOptions);
        }
        return getRemoteDriver(browser.getBrowserVersion(), browserOptions,
                runMode.getRemoteRunMode().getRemoteBrowserUrl());
    }

    private WebDriver getDriverLocally(final AbstractBrowser browser,
                                       final WebDriverManager webDriverManager,
                                       final MutableCapabilities browserOptions) {
        BrowserVersion browserVersion = browser.getBrowserVersion();
        boolean useLatest = BrowserUtil.useLatest(browserVersion);
        if (browser.getRunMode().getLocallyRunMode().getType() == LocallyRunModeType.LOCALLY_IN_DOCKER) {
            return useLatest ? webDriverManager.capabilities(browserOptions)
                            .browserVersion(LATEST).browserInDocker()
                            .create()
                    : webDriverManager.capabilities(browserOptions)
                            .browserVersion(browserVersion.getVersion())
                            .browserInDocker()
                            .create();
        }
        return useLatest ? webDriverManager.capabilities(browserOptions).create()
                : webDriverManager.capabilities(browserOptions).browserVersion(browserVersion.getVersion()).create();
    }

    @SneakyThrows
    private WebDriver getRemoteDriver(final BrowserVersion browserVersion, final MutableCapabilities browserOptions,
                                      final String remoteUrl) {
        if (BrowserUtil.useLatest(browserVersion)) {
            return new RemoteWebDriver(new URL(remoteUrl), browserOptions);
        }
        browserOptions.setCapability(BROWSER_VERSION, browserVersion.getVersion());
        return new RemoteWebDriver(new URL(remoteUrl), browserOptions);
    }

    private void setCapabilities(final Capabilities capabilities, final MutableCapabilities driverOptions) {
        if (capabilities != null) {
            capabilities.getCapability()
                    .forEach(cap -> driverOptions.setCapability(cap.getCapabilityName(), cap.getValue()));
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
            chromeOptions.setHeadless(browser.isSetHeadless());
            setCapabilities(browser.getCapabilities(), chromeOptions);
            BrowserOptionsArguments browserOptionsArguments = browser.getChromeOptionsArguments();
            if (browserOptionsArguments != null) {
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
            firefoxOptions.setHeadless(browser.isSetHeadless());
            setCapabilities(browser.getCapabilities(), firefoxOptions);
            BrowserOptionsArguments browserOptionsArguments = browser.getFirefoxOptionsArguments();
            if (browserOptionsArguments != null) {
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
            edgeOptions.setHeadless(browser.isSetHeadless());
            setCapabilities(browser.getCapabilities(), edgeOptions);
            BrowserOptionsArguments browserOptionsArguments = browser.getEdgeOptionsArguments();
            if (browserOptionsArguments != null) {
                edgeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return edgeOptions;
        }
    }

    private class SafariDriverInitializer implements WebDriverInitializer<Safari> {

        @Override
        public WebDriver init(final Safari browser) {
            return getWebDriver(browser, getSafariOptions(browser), new SafariDriverManager());
        }

        private SafariOptions getSafariOptions(final Safari browser) {
            SafariOptions safariOptions = new SafariOptions();
            setCapabilities(browser.getCapabilities(), safariOptions);
            return safariOptions;
        }
    }

    private class OperaDriverInitializer implements WebDriverInitializer<Opera> {

        @Override
        public WebDriver init(final Opera browser) {
            return getWebDriver(browser, getOperaOptions(browser), new OperaDriverManager());
        }

        private OperaOptions getOperaOptions(final Opera browser) {
            OperaOptions operaOptions = new OperaOptions();
            setCapabilities(browser.getCapabilities(), operaOptions);
            BrowserOptionsArguments browserOptionsArguments = browser.getOperaOptionsArguments();
            if (browserOptionsArguments != null) {
                operaOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return operaOptions;
        }
    }

    private interface BrowserPredicate extends Predicate<AbstractBrowser> { }

    private interface WebDriverFunction extends Function<AbstractBrowser, WebDriver> { }
}
