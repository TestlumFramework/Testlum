package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.BROWSER_INFO;

@UtilityClass
public class BrowserUtil {

    public List<AbstractBrowser> filterEnabledBrowsers() {
        return GlobalTestConfigurationProvider.getBrowsers().stream()
                .filter(AbstractBrowser::isEnable)
                .collect(Collectors.toList());
    }

    public void manageWindowSize(final AbstractBrowser browser, final WebDriver webDriver) {
        String browserWindowSize = browser.getBrowserWindowSize();
        if (StringUtils.isNotEmpty(browserWindowSize)) {
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
        if (Objects.nonNull(browser.getBrowserType().getRemoteBrowser())) {
            return BrowserType.REMOTE;
        }
        if (Objects.nonNull(browser.getBrowserType().getBrowserInDocker())) {
            return BrowserType.IN_DOCKER;
        }
        if (Objects.nonNull(browser.getBrowserType().getBrowserStack())) {
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
        return StringUtils.isEmpty(version) ? "no version specified (the latest version is used)" : version;
    }

    public enum BrowserType {
        LOCAL("local browser"),
        REMOTE("remote browser"),
        IN_DOCKER("browser in docker"),
        BROWSER_STACK("browserStack");

        private final String typeName;

        BrowserType(final String typeName) {
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }
    }
}
