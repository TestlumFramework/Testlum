package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.SeleniumConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;

import com.knubisoft.e2e.testing.model.global_config.WebDriverOptions;
import com.knubisoft.e2e.testing.model.global_config.WebDriverVersion;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class WebDriverUtil {

    private final String HEADLESS_ARGUMENT = "--headless";
    private final String LATEST_VERSION = "latest";
    private final String BROWSER_NOT_SUPPORTED = "Browser not supported";

    public boolean isLocalRunMode() {
        return GlobalTestConfigurationProvider.getWebDriverSettings().getWebDriverRunMode().getLocally() != null;
    }

    public void setUpDriver(final Browser browser, final WebDriverVersion webDriverVersion) {
        if (GlobalTestConfigurationProvider.getWebDriverSettings().getWebDriverRunMode().getLocally().isInDocker()) {
            setUpLocallyInDocker(browser, getVersion(webDriverVersion));
        } else {
            setUpLocally(browser, getVersion(webDriverVersion));
        }
    }

    public List<String> getOptions(final WebDriverOptions webDriverOptions) {
        List<String> result = new ArrayList<>();
        List<String> options = webDriverOptions.getOption();
        if (!webDriverOptions.isDisplayExecution()) {
            result.add(HEADLESS_ARGUMENT);
        }
        if (CollectionUtils.isNotEmpty(options)) {
            result.addAll(options);
        }
        return result;
    }

    public void manageWindowSize(final WebDriver driver) {
        Dimension dimension = new Dimension(SeleniumConstant.SCREEN_WIDTH, SeleniumConstant.SCREEN_HEIGHT);
        driver.manage().window().setSize(dimension);
        driver.manage().window().maximize();
        driver.get(GlobalTestConfigurationProvider.provide().getUi().getBaseUrl());
    }

    private void setUpLocally(final Browser browser, final  String webDriverVersion) {
        if (webDriverVersion.equals(LATEST_VERSION)) {
            setUpLocallyLastVersion(browser);
        } else {
            setUpLocallyWithVersion(browser, webDriverVersion);
        }
    }

    private void setUpLocallyWithVersion(final Browser browser, final String webDriverVersion) {
        switch (browser) {
            case CHROME: WebDriverManager.chromedriver().browserVersion(webDriverVersion).setup();
                break;
            case FIREFOX: WebDriverManager.firefoxdriver().browserVersion(webDriverVersion).setup();
                break;
            case EDGE: WebDriverManager.edgedriver().browserVersion(webDriverVersion).setup();
                break;
            case OPERA: WebDriverManager.operadriver().browserVersion(webDriverVersion).setup();
                break;
            case SAFARI: WebDriverManager.safaridriver().browserVersion(webDriverVersion).setup();
                break;
            default: throw new DefaultFrameworkException(BROWSER_NOT_SUPPORTED);
        }
    }

    private void setUpLocallyLastVersion(final Browser browser) {
        switch (browser) {
            case CHROME: WebDriverManager.chromedriver().setup();
                break;
            case FIREFOX: WebDriverManager.firefoxdriver().setup();
                break;
            case EDGE: WebDriverManager.edgedriver().setup();
                break;
            case OPERA: WebDriverManager.operadriver().setup();
                break;
            case SAFARI: WebDriverManager.safaridriver().setup();
                break;
            default: throw new DefaultFrameworkException(BROWSER_NOT_SUPPORTED);
        }
    }

    public WebDriver setUpLocallyInDocker(final Browser browser, final String webDriverVersion) {
        switch (browser) {
            case CHROME:
                return WebDriverManager.chromedriver().browserInDocker().browserVersion(webDriverVersion).create();
            case FIREFOX:
                return WebDriverManager.firefoxdriver().browserInDocker().browserVersion(webDriverVersion).create();
            case EDGE:
                return WebDriverManager.edgedriver().browserInDocker().browserVersion(webDriverVersion).create();
            case OPERA:
                return WebDriverManager.operadriver().browserInDocker().browserVersion(webDriverVersion).create();
            case SAFARI:
                return WebDriverManager.safaridriver().browserInDocker().browserVersion(webDriverVersion).create();
            default: throw new DefaultFrameworkException(BROWSER_NOT_SUPPORTED);
        }
    }

    private String getVersion(final WebDriverVersion driverVersion) {
        return driverVersion.isUseLatest() ? LATEST_VERSION : Objects.requireNonNull(driverVersion.getVersion());
    }

    public enum Browser {
        CHROME,FIREFOX,EDGE,SAFARI,OPERA
    }
}
