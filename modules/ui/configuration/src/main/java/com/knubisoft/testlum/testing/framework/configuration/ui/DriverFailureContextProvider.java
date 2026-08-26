package com.knubisoft.testlum.testing.framework.configuration.ui;

import com.knubisoft.testlum.testing.framework.UIConfiguration;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.BrowserUtil;
import com.knubisoft.testlum.testing.framework.util.DriverFailureContext;
import com.knubisoft.testlum.testing.framework.util.SeleniumDriverUtil;
import com.knubisoft.testlum.testing.framework.util.UiDriverKind;
import com.knubisoft.testlum.testing.framework.validator.UiConfigPathResolver;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverFailureContextProvider {

    private final UiConfigPathResolver configPathResolver;
    private final SeleniumDriverUtil seleniumDriverUtil;
    private final BrowserUtil browserUtil;
    private final UIConfiguration uiConfigs;

    public DriverFailureContext forWeb(final AbstractBrowser browser) {
        BrowserUtil.BrowserType browserType = quietly(() -> browserUtil.getBrowserType(browser));
        return baseContext(UiDriverKind.WEB, browser.getAlias(), browser.getClass().getSimpleName())
                .connectionName(Objects.nonNull(browserType) ? browserType.getTypeName() : null)
                .serverUrl(quietly(() -> webServerUrl(browser, browserType)))
                .build();
    }

    public DriverFailureContext forDevice(final UiDriverKind kind,
                                          final AbstractDevice device,
                                          final ConnectionType connectionType,
                                          final String serverUrl) {
        return baseContext(kind, device.getAlias(), platformOf(device))
                .connectionName(connectionName(connectionType))
                .serverUrl(serverUrl)
                .build();
    }

    private DriverFailureContext.DriverFailureContextBuilder baseContext(final UiDriverKind kind,
                                                                         final String alias,
                                                                         final String qualifier) {
        String env = EnvManager.currentEnv();
        return DriverFailureContext.builder()
                .kind(kind)
                .alias(alias)
                .qualifier(qualifier)
                .env(env)
                .configPath(StringUtils.defaultIfBlank(quietly(() -> configPathResolver.resolve(env)),
                        ExceptionMessage.UI_CONFIG_FILENAME));
    }

    private <T> T quietly(final Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.debug("Could not collect driver failure context", e);
            return null;
        }
    }

    private String platformOf(final AbstractDevice device) {
        return Objects.nonNull(device.getPlatformName()) ? device.getPlatformName().value() : null;
    }

    private String connectionName(final ConnectionType connectionType) {
        if (Objects.isNull(connectionType)) {
            return null;
        }
        if (Objects.nonNull(connectionType.getAppiumServer())) {
            return LogMessage.CONNECTION_APPIUM_SERVER;
        }
        return Objects.nonNull(connectionType.getBrowserStack()) ? LogMessage.CONNECTION_BROWSER_STACK : null;
    }

    private String webServerUrl(final AbstractBrowser browser, final BrowserUtil.BrowserType browserType) {
        if (BrowserUtil.BrowserType.REMOTE == browserType) {
            return browser.getBrowserType().getRemoteBrowser().getRemoteBrowserURL();
        }
        if (BrowserUtil.BrowserType.BROWSER_STACK == browserType) {
            return browserStackUrl();
        }
        return null;
    }

    private String browserStackUrl() {
        return quietly(() -> seleniumDriverUtil.getBrowserStackUrl(uiConfigs.get(EnvManager.currentEnv())));
    }
}
