package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.model.global_config.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.managers.ChromeDriverManager;
import io.github.bonigarcia.wdm.managers.EdgeDriverManager;
import io.github.bonigarcia.wdm.managers.FirefoxDriverManager;
import io.github.bonigarcia.wdm.managers.SafariDriverManager;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DRIVER_INITIALIZER_NOT_FOUND;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class WebDriverFactory {
    private static final String DEFAULT_DOCKER_SCREEN_COLORS_DEPTH = "x24";

    public WebDriver createDriver(final AbstractBrowser browser, final Path downloadPath) {
        WebDriverInitializer<?> initializer = getWebDriverInitializer(browser, downloadPath);

        @SuppressWarnings("unchecked")
        WebDriver webDriver = ((WebDriverInitializer<AbstractBrowser>) initializer).init(browser);

        BrowserUtil.manageWindowSize(browser, webDriver);
        Web settings = GlobalTestConfigurationProvider.getWebSettings(EnvManager.currentEnv());
//        int secondsToWait = settings.getBrowserSettings().getElementAutowait().getSeconds();
//        webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(secondsToWait));
        webDriver.get(settings.getBaseUrl());
        return webDriver;
    }

    private static @NotNull WebDriverInitializer<?> getWebDriverInitializer(AbstractBrowser browser, Path downloadPath) {
        WebDriverInitializer<?> initializer;

        if (browser instanceof Chrome) {
            initializer = new ChromeDriverInitializer(downloadPath);
        } else if (browser instanceof Firefox) {
            initializer = new FirefoxDriverInitializer(downloadPath);
        } else if (browser instanceof Edge) {
            initializer = new EdgeDriverInitializer(downloadPath);
        } else if (browser instanceof Safari) {
            initializer = new SafariDriverInitializer();
        } else {
            throw new DefaultFrameworkException(DRIVER_INITIALIZER_NOT_FOUND);
        }
        return initializer;
    }

    private WebDriver getWebDriver(final AbstractBrowser browser,
                                   final MutableCapabilities browserOptions,
                                   final WebDriverManager driverManager) {
        setCapabilities(browser, browserOptions);
        switch (BrowserUtil.getBrowserType(browser)) {
            case BROWSER_STACK:
                return getBrowserStackDriver(browser, browserOptions);
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
    private WebDriver getBrowserStackDriver(final AbstractBrowser browser,
                                            final MutableCapabilities browserOptions) {
        BrowserStackWeb browserStack = browser.getBrowserType().getBrowserStack();
        browserOptions.setCapability("appium:browserstack.local", Boolean.TRUE);
        browserOptions.setCapability("appium:browserstack.use_w3c", Boolean.TRUE);
        browserOptions.setCapability(CapabilityType.BROWSER_NAME, browser.getClass().getSimpleName());
        browserOptions.setCapability(CapabilityType.BROWSER_VERSION, browserStack.getBrowserVersion());
        browserOptions.setCapability("os", browserStack.getOs());
        browserOptions.setCapability("osVersion", browserStack.getOsVersion());
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
        private final Path downloadPath;

        public ChromeDriverInitializer(Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        public WebDriver init(final Chrome browser) {
            return getWebDriver(browser, getChromeOptions(browser), new ChromeDriverManager());
        }

        private ChromeOptions getChromeOptions(final Chrome browser) {
            ChromeOptions chromeOptions = new ChromeOptions();

            if (nonNull(downloadPath)) {
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("download.default_directory", downloadPath.toAbsolutePath().toString());
                prefs.put("download.prompt_for_download", false);
                prefs.put("download.directory_upgrade", true);
                prefs.put("safebrowsing.enabled", true);
                prefs.put("profile.default_content_settings.popups", 0);
                chromeOptions.setExperimentalOption("prefs", prefs);
            }

            if (browser.isHeadlessMode()) {
                chromeOptions.addArguments("--headless=new");
            }
            BrowserOptionsArguments browserOptionsArguments = browser.getChromeOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                chromeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            chromeOptions.setExperimentalOption("useAutomationExtension", false);
            return chromeOptions;
        }
    }

    private class FirefoxDriverInitializer implements WebDriverInitializer<Firefox> {
        private final Path downloadPath;

        public FirefoxDriverInitializer(Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        public WebDriver init(final Firefox browser) {
            return getWebDriver(browser, getFirefoxOptions(browser), new FirefoxDriverManager());
        }

        private FirefoxOptions getFirefoxOptions(final Firefox browser) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            if (nonNull(downloadPath)) {
                firefoxOptions.addPreference("browser.download.folderList", 2);
                firefoxOptions.addPreference("browser.download.dir", downloadPath.toAbsolutePath().toString());
                firefoxOptions.addPreference("browser.download.useDownloadDir", true);
                firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk",
                        "application/pdf,application/zip,application/octet-stream,text/csv,image/jpeg,image/png,application/json");
                firefoxOptions.addPreference("pdfjs.disabled", true);
            }
            if (browser.isHeadlessMode()) {
                firefoxOptions.addArguments("-headless");
            }
            BrowserOptionsArguments browserOptionsArguments = browser.getFirefoxOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                firefoxOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return firefoxOptions;
        }
    }

    private class EdgeDriverInitializer implements WebDriverInitializer<Edge> {
        private final Path downloadPath;

        public EdgeDriverInitializer(Path downloadPath) {
            this.downloadPath = downloadPath;
        }

        public WebDriver init(final Edge browser) {
            return getWebDriver(browser, getEdgeOptions(browser), new EdgeDriverManager());
        }

        private EdgeOptions getEdgeOptions(final Edge browser) {
            EdgeOptions edgeOptions = new EdgeOptions();
            if (nonNull(downloadPath)) {
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("download.default_directory", downloadPath.toAbsolutePath().toString());
                prefs.put("download.prompt_for_download", false);
                edgeOptions.setExperimentalOption("prefs", prefs);
            }
            if (browser.isHeadlessMode()) {
                edgeOptions.addArguments("--headless=new");
            }
            BrowserOptionsArguments browserOptionsArguments = browser.getEdgeOptionsArguments();
            if (nonNull(browserOptionsArguments)) {
                edgeOptions.addArguments(browserOptionsArguments.getArgument());
            }
            return edgeOptions;
        }
    }

    private class SafariDriverInitializer implements WebDriverInitializer<Safari> {

        public WebDriver init(final Safari browser) {
            return getWebDriver(browser, new SafariOptions(), new SafariDriverManager());
        }
    }
}
