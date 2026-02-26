package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BROWSER_INFO;

@Component
@RequiredArgsConstructor
public class BrowserUtil {

    private final EnvironmentLoader environmentLoader;
    private final UiConfig uiConfig;

    public List<AbstractBrowser> filterDefaultEnabledBrowsers() {
        Web web = uiConfig.getWeb();
        return Objects.nonNull(web)
                ? web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari().stream()
                .filter(AbstractBrowser::isEnabled).toList()
                : Collections.emptyList();
    }

    @SneakyThrows
    public Optional<AbstractBrowser> getBrowserBy(final String env, final String browserAlias) {
        return StringUtils.isBlank(browserAlias)
                ? Optional.empty()
                : environmentLoader.getWebSettings(env).
                flatMap(e-> finEnabledByAlias(e, browserAlias));
    }

    private Optional<AbstractBrowser> finEnabledByAlias(Web web, String browserAlias) {
        return web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari().stream()
                .filter(browser -> browser.isEnabled() && browser.getAlias().equalsIgnoreCase(browserAlias))
                .findFirst();
    }

    public void manageWindowSize(final AbstractBrowser browser, final WebDriver webDriver) {
        String browserWindowSize = browser.getBrowserWindowSize();
        if (StringUtils.isNotBlank(browserWindowSize)) {
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
        return StringUtils.isBlank(version) ? "No browser version specified (the latest version is used)" : version;
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
