package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.ConnectionTemplateImpl;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
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
import com.knubisoft.testlum.testing.model.global_config.RemoteBrowser;
import com.knubisoft.testlum.testing.model.global_config.Safari;
import com.knubisoft.testlum.testing.model.global_config.ScreenRecording;
import com.knubisoft.testlum.testing.model.global_config.Web;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.SafariDriverManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.safari.SafariOptions;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;


@Component
@Slf4j
@RequiredArgsConstructor
public class WebDriverFactory {

    private static final String DEFAULT_DOCKER_SCREEN_COLORS_DEPTH = "x24";
    private static final int MAX_TIMEOUT_SECONDS = 60;

    private final SeleniumDriverUtil seleniumDriverUtil;
    private final EnvironmentLoader environmentLoader;
    private final BrowserUtil browserUtil;
    private final UIConfiguration uiConfigs;

    private final Map<BrowserPredicate, WebDriverFunction> driverInitializerMap = Map.of(
            browser -> browser instanceof Chrome, (b, path) -> new ChromeDriverInitializer(path).init((Chrome) b),
            browser -> browser instanceof Firefox, (b, path) -> new FirefoxDriverInitializer(path).init((Firefox) b),
            browser -> browser instanceof Safari, (b, path) -> new SafariDriverInitializer().init((Safari) b),
            browser -> browser instanceof Edge, (b, path) -> new EdgeDriverInitializer(path).init((Edge) b));

    public WebDriver createDriver(final AbstractBrowser browser, final Path downloadPath) {
        ConnectionTemplate connectionTemplate = new ConnectionTemplateImpl();
        String connectionInfo = String.format(LogMessage.CONNECTION_INTEGRATION_DATA,
                browser.getClass().getSimpleName(), browser.getAlias());
        return connectionTemplate.executeWithRetry(
                connectionInfo,
                ConnectionTemplate.DEFAULT_ATTEMPTS,
                () -> initializeDriver(browser, downloadPath),
                forWebDriver(browser),
                this::safeQuitDriver);
    }

    private WebDriver initializeDriver(final AbstractBrowser browser, final Path downloadPath) {
        return driverInitializerMap.entrySet().stream()
                .filter(function -> function.getKey().test(browser))
                .findFirst()
                .map(function -> function.getValue().apply(browser, downloadPath))
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.DRIVER_INITIALIZER_NOT_FOUND));
    }

    private void safeQuitDriver(final WebDriver driver) {
        try {
            driver.quit();
        } catch (final Exception e) {
            throw new DefaultFrameworkException("Failed to quit WebDriver: ".concat(e.getMessage()));
        }
    }

    private IntegrationHealthCheck<WebDriver> forWebDriver(final AbstractBrowser browser) {
        return webDriver -> {
            try {
                safeInitWebdriverWithTimeouts(browser, webDriver);
            } catch (Exception e) {
                log.error("Failed to initialize driver or reach base URL within {}s", MAX_TIMEOUT_SECONDS);
                if (webDriver != null) {
                    webDriver.quit();
                }
                throw new DefaultFrameworkException(e.getMessage());
            }
        };
    }

    private void safeInitWebdriverWithTimeouts(final AbstractBrowser browser, final WebDriver webDriver) {
        webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
        webDriver.manage().timeouts().scriptTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
        browserUtil.manageWindowSize(browser, webDriver);
        Optional<Web> settings = environmentLoader.getCurrentEnvWebSettings();
        if (settings.isPresent()) {
            String url = settings.get().getBaseUrl();
            log.debug("Navigating to base URL: {}", url);
            webDriver.get(url);
        }
    }

    private WebDriver getWebDriver(final AbstractBrowser browser,
                                   final MutableCapabilities browserOptions,
                                   final WebDriverManager driverManager) {
        setCapabilities(browser, browserOptions);
        return switch (browserUtil.getBrowserType(browser)) {
            case BROWSER_STACK -> getBrowserStackDriver(browser, browserOptions);
            case REMOTE -> getRemoteDriver(browser.getBrowserType().getRemoteBrowser(), browserOptions);
            case IN_DOCKER -> {
                WebDriverManager webDriverManager = setScreenResolution(browser, driverManager);
                yield getBrowserInDocker(browser.getBrowserType().getBrowserInDocker(),
                        browserOptions, webDriverManager);
            }
            default -> getLocalDriver(browser.getBrowserType().getLocalBrowser(), browserOptions, driverManager);
        };
    }

    private WebDriver getBrowserStackDriver(final AbstractBrowser browser,
                                            final MutableCapabilities browserOptions) {
        BrowserStackWeb browserStack = browser.getBrowserType().getBrowserStack();
        browserOptions.setCapability("appium:browserstack.local", Boolean.TRUE);
        browserOptions.setCapability("appium:browserstack.use_w3c", Boolean.TRUE);
        browserOptions.setCapability(CapabilityType.BROWSER_NAME, browser.getClass().getSimpleName());
        browserOptions.setCapability(CapabilityType.BROWSER_VERSION, browserStack.getBrowserVersion());
        browserOptions.setCapability("os", browserStack.getOs());
        browserOptions.setCapability("osVersion", browserStack.getOsVersion());
        String url = seleniumDriverUtil.getBrowserStackUrl(uiConfigs.get(EnvManager.currentEnv()));
        return new RemoteWebDriver(toURL(url), browserOptions);
    }

    private WebDriver getRemoteDriver(final RemoteBrowser remoteBrowserSettings,
                                      final MutableCapabilities browserOptions) {
        String url = remoteBrowserSettings.getRemoteBrowserURL();
        log.info("Connecting to Remote Browser at: {}", url);
        ClientConfig config = ClientConfig.defaultConfig()
                .connectionTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(MAX_TIMEOUT_SECONDS));
        try {
            return RemoteWebDriver.builder().address(toURL(url))
                    .oneOf(browserOptions).config(config).build();
        } catch (Exception e) {
            throw new DefaultFrameworkException("Unable to connect to remote browser with cause:" + e.getMessage());
        }
    }

    private URL toURL(final String url) {
        return seleniumDriverUtil.toURL(url);
    }

    private WebDriverManager setScreenResolution(final AbstractBrowser browser,
                                                 final WebDriverManager driverManager) {
        return StringUtils.isNotBlank(browser.getBrowserWindowSize())
                ? driverManager.browserInDocker().dockerScreenResolution(browser.getBrowserWindowSize()
                + DEFAULT_DOCKER_SCREEN_COLORS_DEPTH) : driverManager.browserInDocker();
    }

    private WebDriver getBrowserInDocker(final BrowserInDocker browserInDockerSettings,
                                         final MutableCapabilities browserOptions,
                                         final WebDriverManager driverManager) {
        log.debug("Configuring Docker container for {}", browserOptions.getBrowserName());
        driverManager.timeout(MAX_TIMEOUT_SECONDS);

        String dockerNetwork = browserInDockerSettings.getDockerNetwork();
        ScreenRecording screenRecordingSettings = browserInDockerSettings.getScreenRecording();
        driverManager.capabilities(browserOptions).browserVersion(browserInDockerSettings.getBrowserVersion());
        if (StringUtils.isNotBlank(dockerNetwork)) {
            driverManager.dockerNetwork(dockerNetwork);
        }
        if (Objects.nonNull(screenRecordingSettings) && screenRecordingSettings.isEnabled()) {
            driverManager.enableRecording().dockerRecordingOutput(screenRecordingSettings.getOutputFolder());
        }
        return browserInDockerSettings.isEnableVNC() ? driverManager.enableVnc().create() : driverManager.create();
    }

    private WebDriver getLocalDriver(final LocalBrowser localBrowserSettings,
                                     final MutableCapabilities browserOptions,
                                     final WebDriverManager driverManager) {
        log.debug("Setting up local {} driver", browserOptions.getBrowserName());
        String driverVersion = localBrowserSettings.getDriverVersion();
        if (StringUtils.isNotBlank(driverVersion)) {
            driverManager.driverVersion(driverVersion);
        }
        driverManager.timeout(MAX_TIMEOUT_SECONDS);
        return driverManager.capabilities(browserOptions).create();
    }

    private void setCapabilities(final AbstractBrowser browser, final MutableCapabilities driverOptions) {
        Capabilities capabilities = browser.getCapabilities();
        if (Objects.nonNull(capabilities)) {
            capabilities.getCapability().forEach(cap -> driverOptions.setCapability(cap.getName(), cap.getValue()));
        }
    }

    private interface WebDriverInitializer {}

    private class ChromeDriverInitializer implements WebDriverInitializer {
        private final Path downloadPath;

        private ChromeDriverInitializer(final Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        private WebDriver init(final Chrome browser) {
            return getWebDriver(browser, getChromeOptions(browser), new ChromeDriverManager());
        }

        private ChromeOptions getChromeOptions(final Chrome browser) {
            ChromeOptions chromeOptions = new ChromeOptions();
            if (Objects.nonNull(downloadPath)) {
                setChromeOptions(chromeOptions);
            }
            if (browser.isHeadlessMode()) {
                chromeOptions.addArguments("--headless=new");
            }
            addChromeArgument(browser, chromeOptions);
            chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
            return chromeOptions;
        }

        private void setChromeOptions(final ChromeOptions chromeOptions) {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", downloadPath.toAbsolutePath().toString());
            prefs.put("download.prompt_for_download", false);
            prefs.put("download.directory_upgrade", true);
            prefs.put("safebrowsing.enabled", true);
            prefs.put("profile.default_content_settings.popups", 0);
            chromeOptions.setExperimentalOption("prefs", prefs);
        }

        private void addChromeArgument(final Chrome browser, final ChromeOptions chromeOptions) {
            BrowserOptionsArguments browserOptionsArguments = browser.getChromeOptionsArguments();
            if (Objects.nonNull(browserOptionsArguments)) {
                chromeOptions.addArguments(browserOptionsArguments.getArgument());
            }
        }
    }

    private class FirefoxDriverInitializer implements WebDriverInitializer {
        private final Path downloadPath;

        private FirefoxDriverInitializer(final Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        private WebDriver init(final Firefox browser) {
            return getWebDriver(browser, getFirefoxOptions(browser), new FirefoxDriverManager());
        }

        private FirefoxOptions getFirefoxOptions(final Firefox browser) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            if (Objects.nonNull(downloadPath)) {
                setFirefoxOptions(firefoxOptions);
            }
            if (browser.isHeadlessMode()) {
                firefoxOptions.addArguments("-headless");
            }
            BrowserOptionsArguments browserOptionsArguments = browser.getFirefoxOptionsArguments();
            if (Objects.nonNull(browserOptionsArguments)) {
                firefoxOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return firefoxOptions;
        }

        private void setFirefoxOptions(final FirefoxOptions firefoxOptions) {
            firefoxOptions.addPreference("browser.download.folderList", 2);
            firefoxOptions.addPreference("browser.download.dir", downloadPath.toAbsolutePath().toString());
            firefoxOptions.addPreference("browser.download.useDownloadDir", true);
            firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk",
                    "application/pdf,application/zip,application/octet-stream,"
                            + "text/csv,image/jpeg,image/png,application/json");
            firefoxOptions.addPreference("pdfjs.disabled", true);
        }
    }

    private class EdgeDriverInitializer implements WebDriverInitializer {
        private final Path downloadPath;

        private EdgeDriverInitializer(final Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        private WebDriver init(final Edge browser) {
            return getWebDriver(browser, getEdgeOptions(browser), new EdgeDriverManager());
        }

        private EdgeOptions getEdgeOptions(final Edge browser) {
            EdgeOptions edgeOptions = new EdgeOptions();
            if (Objects.nonNull(downloadPath)) {
                setEdgeOptions(edgeOptions);
            }
            if (browser.isHeadlessMode()) {
                edgeOptions.addArguments("--headless=new");
            }
            BrowserOptionsArguments browserOptionsArguments = browser.getEdgeOptionsArguments();
            if (Objects.nonNull(browserOptionsArguments)) {
                edgeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return edgeOptions;
        }

        private void setEdgeOptions(final EdgeOptions edgeOptions) {
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("download.default_directory", downloadPath.toAbsolutePath().toString());
            prefs.put("download.prompt_for_download", false);
            edgeOptions.setExperimentalOption("prefs", prefs);
        }
    }

    private class SafariDriverInitializer implements WebDriverInitializer {

        private WebDriver init(final Safari browser) {
            return getWebDriver(browser, new SafariOptions(), new SafariDriverManager());
        }
    }

    private interface BrowserPredicate extends Predicate<AbstractBrowser> { }
    private interface WebDriverFunction extends BiFunction<AbstractBrowser, Path, WebDriver> { }
}
