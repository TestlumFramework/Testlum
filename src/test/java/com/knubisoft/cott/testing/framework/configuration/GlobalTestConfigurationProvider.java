package com.knubisoft.cott.testing.framework.configuration;

import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.Web;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static List<AbstractBrowser> getBrowsers() {
        return getBrowserSettings().getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari();
    }

    public static List<NativeDevice> getNativeDevices() {
        return GlobalTestConfigurationProvider.provide().getNative().getDeviceSettings().getDevices().getDevice();
    }

    public static List<MobilebrowserDevice> getMobilebrowserDevices() {
        return getMobilebrowserSettings().getDeviceSettings().getDevices().getDevice();
    }

    public static Web getBrowserSettings() {
       return GlobalTestConfigurationProvider.provide().getWeb();
    }

    public static Mobilebrowser getMobilebrowserSettings() {
        return GlobalTestConfigurationProvider.provide().getMobilebrowser();
    }

    public static Native getNativeSettings() {
        return GlobalTestConfigurationProvider.provide().getNative();
    }

    public static Integrations getIntegrations() {
        return GlobalTestConfigurationProvider.provide().getIntegrations();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration().process(TestResourceSettings.getInstance().getConfigFile());
    }
}
