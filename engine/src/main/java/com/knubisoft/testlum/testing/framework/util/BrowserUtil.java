package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BROWSER_INFO;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class BrowserUtil {

    public List<AbstractBrowser> filterDefaultEnabledBrowsers() {
        Web web = GlobalTestConfigurationProvider.get().getDefaultUiConfigs().getWeb();
        return nonNull(web)
                ? web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari().stream()
                .filter(AbstractBrowser::isEnabled).collect(Collectors.toList())
                : Collections.emptyList();
    }

    public Optional<AbstractBrowser> getBrowserBy(final String env, final String browserAlias) {
        return isBlank(browserAlias)
                ? Optional.empty()
                : GlobalTestConfigurationProvider.get().getWebSettings(env)
                .getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari().stream()
                .filter(browser -> browser.isEnabled() && browser.getAlias().equalsIgnoreCase(browserAlias))
                .findFirst();
    }

    public void manageWindowSize(final AbstractBrowser browser, final WebDriver webDriver) {
        String browserWindowSize = browser.getBrowserWindowSize();
        if (isNotBlank(browserWindowSize)) {
            String[] size = browserWindowSize.split(DelimiterConstant.X);
            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            webDriver.manage().window().setSize(new Dimension(width, height));
        }
        if (browser.isMaximizedBrowserWindow()) {
            webDriver.manage().window().maximize();
        }
    }

    public String getBrowserInfo(final AbstractBrowser browser) {
        String browserName = browser.getClass().getSimpleName();
        BrowserType browserType = getBrowserType(browser);
        String browserVersion = getBrowserVersion(browser, browserType);
        return String.format(BROWSER_INFO, browserName, browserType.getTypeName(), browserVersion);
    }

    public BrowserType getBrowserType(final AbstractBrowser browser) {
        if (nonNull(browser.getBrowserType().getRemoteBrowser())) {
            return BrowserType.REMOTE;
        }
        if (nonNull(browser.getBrowserType().getBrowserInDocker())) {
            return BrowserType.IN_DOCKER;
        }
        if (nonNull(browser.getBrowserType().getBrowserStack())) {
            return BrowserType.BROWSER_STACK;
        }
        return BrowserType.LOCAL;
    }

    public String getBrowserVersion(final AbstractBrowser browser, final BrowserType browserType) {
        if (browserType == BrowserType.REMOTE) {
            return browser.getBrowserType().getRemoteBrowser().getBrowserVersion();
        }
        if (browserType == BrowserType.IN_DOCKER) {
            return browser.getBrowserType().getBrowserInDocker().getBrowserVersion();
        }
        if (browserType == BrowserType.BROWSER_STACK) {
            return browser.getBrowserType().getBrowserStack().getBrowserVersion();
        }
        String version = browser.getBrowserType().getLocalBrowser().getDriverVersion();
        return isBlank(version) ? "No browser version specified (the latest version is used)" : version;
    }

    @Getter
    public enum BrowserType {
        LOCAL("local browser"),
        REMOTE("remote browser"),
        IN_DOCKER("browser in docker"),
        BROWSER_STACK("browserStack");

        private final String typeName;

        BrowserType(final String typeName) {
            this.typeName = typeName;
        }

    }
}
