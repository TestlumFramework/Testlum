package com.knubisoft.e2e.testing.framework.configuration;

import com.knubisoft.e2e.testing.framework.parser.XMLParsers;
import com.knubisoft.e2e.testing.model.global_config.AbstractBrowser;
import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.e2e.testing.model.global_config.Integrations;
import com.knubisoft.e2e.testing.model.global_config.WebDriverSettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static List<AbstractBrowser> getWebBrowserVersions() {
        return getWebDriverSettings().getBrowserVersions().getChromeOrFirefoxOrSafari();
    }

    public static WebDriverSettings getWebDriverSettings() {
       return GlobalTestConfigurationProvider.provide().getUi().getWebDriverSettings();
    }

    public static Integrations getIntegrations() {
        return GlobalTestConfigurationProvider.provide().getIntegrations();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration().process(TestResourceSettings.getInstance().getConfigFile());
    }
}
