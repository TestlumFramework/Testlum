package com.knubisoft.e2e.testing.framework;

import com.knubisoft.e2e.testing.framework.constant.SeleniumConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.util.WebDriverUtil;
import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.Chrome;
import com.knubisoft.e2e.testing.model.global_config.Edge;
import com.knubisoft.e2e.testing.model.global_config.Firefox;
import com.knubisoft.e2e.testing.model.global_config.Opera;
import com.knubisoft.e2e.testing.model.global_config.Safari;
import com.knubisoft.e2e.testing.model.global_config.WebDriverOptions;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.DRIVER_INITIALIZER_NOT_FOUND;

@UtilityClass
public class WebDriverFactory {

    private static final Map<WebBrowserPredicate, WebDriverInitializer> DRIVER_INITIALIZER_MAP;

    static {
        final Map<WebBrowserPredicate, WebDriverInitializer> map = new HashMap<>(5);

        map.put(browser -> browser instanceof Chrome, new ChromeDriverInitializer());
        map.put(browser -> browser instanceof Firefox, new FirefoxDriverInitializer());
        map.put(browser -> browser instanceof Opera, new OperaDriverInitializer());
        map.put(browser -> browser instanceof Safari, new EdgeDriverInitializer());
        map.put(browser -> browser instanceof Edge, new EdgeDriverInitializer());

        DRIVER_INITIALIZER_MAP = Collections.unmodifiableMap(map);
    }

    public WebDriver create(final AbstractBrowser webBrowser) {
        WebDriverInitializer driverInitializer = getWebDriverInitializer(webBrowser);
        return WebDriverUtil.isLocalRunMode() ? driverInitializer.initDocker(webBrowser)
                : driverInitializer.initRemote(webBrowser);
    }

    private WebDriverInitializer getWebDriverInitializer(final AbstractBrowser webBrowser) {
        return DRIVER_INITIALIZER_MAP.keySet().stream()
                .filter(key -> key.test(webBrowser))
                .map(DRIVER_INITIALIZER_MAP::get)
                .findFirst().orElseThrow(() -> new DefaultFrameworkException(DRIVER_INITIALIZER_NOT_FOUND));
    }

    private interface WebDriverInitializer {
        WebDriver init(AbstractBrowser webBrowser);

        WebDriver initRemote(AbstractBrowser webBrowser);

        WebDriver initDocker(AbstractBrowser webBrowser);
    }

    private static class ChromeDriverInitializer implements WebDriverInitializer {
        @Override
        public WebDriver init(final AbstractBrowser webBrowser) {
            WebDriverUtil.setUpDriver(WebDriverUtil.Browser.CHROME, webBrowser.getWebDriverVersion());
            ChromeOptions chromeOptions = getChromeOptions(webBrowser.getWebDriverOptions());
            ChromeDriver chromeDriver = new ChromeDriver(chromeOptions);
            WebDriverUtil.manageWindowSize(chromeDriver);
            return chromeDriver;
        }

        @SneakyThrows
        @Override
        public WebDriver initRemote(final AbstractBrowser webBrowser) {
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(webBrowser);
            WebDriver webDriver = new RemoteWebDriver(new URL(SeleniumConstant.REMOTE_URL), cap);
            WebDriverUtil.manageWindowSize(webDriver);
            return webDriver;
        }

        @Override
        public WebDriver initDocker(AbstractBrowser webBrowser) {
            WebDriver driver = WebDriverUtil.setUpLocallyInDocker(WebDriverUtil.Browser.CHROME, "latest");
            WebDriverUtil.manageWindowSize(driver);
            return driver;
        }

        private ChromeOptions getChromeOptions(final WebDriverOptions webDriverOptions) {
            ChromeOptions chromeOptions = new ChromeOptions();
            List<String> options = WebDriverUtil.getOptions(webDriverOptions);
            chromeOptions.addArguments(options);
            return chromeOptions;
        }

        @NotNull
        private DesiredCapabilities getCapabilitiesAndConfigureDriver(final AbstractBrowser webBrowser) {
            final ChromeOptions options = new ChromeOptions();
            final List<String> optionsList = WebDriverUtil.getOptions(webBrowser.getWebDriverOptions());
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

    private static class FirefoxDriverInitializer implements WebDriverInitializer {
        @Override
        public WebDriver init(final AbstractBrowser webBrowser) {
            WebDriverUtil.setUpDriver(WebDriverUtil.Browser.FIREFOX, webBrowser.getWebDriverVersion());
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(webBrowser);
            FirefoxDriver firefoxDriver = new FirefoxDriver(cap);
            WebDriverUtil.manageWindowSize(firefoxDriver);
            return firefoxDriver;
        }

        @SneakyThrows
        @Override
        public WebDriver initRemote(final AbstractBrowser webBrowser) {
            DesiredCapabilities cap = getCapabilitiesAndConfigureDriver(webBrowser);
            WebDriver webDriver = new RemoteWebDriver(new URL(SeleniumConstant.REMOTE_URL), cap);
            WebDriverUtil.manageWindowSize(webDriver);
            return webDriver;
        }

        @NotNull
        private DesiredCapabilities getCapabilitiesAndConfigureDriver(final AbstractBrowser webBrowser) {
            FirefoxOptions options = new FirefoxOptions();
            List<String> optionsList = WebDriverUtil.getOptions(webBrowser.getWebDriverOptions());
            if (CollectionUtils.isNotEmpty(optionsList)) {
                options.addArguments(optionsList.toArray(new String[0]));
            }
            DesiredCapabilities cap = getDesiredCapabilities(options);
            return cap;
        }

        @Override
        public WebDriver initDocker(AbstractBrowser webBrowser) {
            return null;
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

    private static class OperaDriverInitializer implements WebDriverInitializer {

        @Override
        public WebDriver init(final AbstractBrowser webBrowser) {
            throw new NotImplementedException();
        }

        @Override
        public WebDriver initRemote(final AbstractBrowser webBrowser) {
            throw new NotImplementedException();
        }

        @Override
        public WebDriver initDocker(AbstractBrowser webBrowser) {
            return null;
        }
    }

    private static class EdgeDriverInitializer implements WebDriverInitializer {

        @Override
        public WebDriver init(final AbstractBrowser webBrowser) {
            throw new NotImplementedException();
        }

        @Override
        public WebDriver initRemote(final AbstractBrowser webBrowser) {
            throw new NotImplementedException();
        }

        @Override
        public WebDriver initDocker(AbstractBrowser webBrowser) {
            return null;
        }
    }

    private interface WebBrowserPredicate extends Predicate<AbstractBrowser> { }
}
