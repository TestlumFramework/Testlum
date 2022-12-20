package com.knubisoft.cott.testing.framework.configuration;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.Env;
import com.knubisoft.cott.testing.model.global_config.Environment;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.Web;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();
    private static final Environment ENVIRONMENT = initEnv();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static Environment provideEnv() {
        return ENVIRONMENT;
    }

    public static List<AbstractBrowser> getBrowsers() {
        if (getBrowserSettings() != null) {
            return getBrowserSettings().getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari();
        }
        return Collections.emptyList();
    }

    public static List<NativeDevice> getNativeDevices() {
        if (getNativeSettings() != null) {
            return getNativeSettings().getDeviceSettings().getDevices().getAndroidOrIos();
        }
        return Collections.emptyList();
    }

    public static List<MobilebrowserDevice> getMobilebrowserDevices() {
        if (getMobilebrowserSettings() != null) {
            return getMobilebrowserSettings().getDeviceSettings().getDevices().getDevice();
        }
        return Collections.emptyList();
    }

    public static Web getBrowserSettings() {
       return GlobalTestConfigurationProvider.provideEnv().getWeb();
    }

    public static Mobilebrowser getMobilebrowserSettings() {
        return GlobalTestConfigurationProvider.provideEnv().getMobilebrowser();
    }

    public static Native getNativeSettings() {
        return GlobalTestConfigurationProvider.provideEnv().getNative();
    }

    public static Integrations getIntegrations() {
        return GlobalTestConfigurationProvider.provideEnv().getIntegrations();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration().process(TestResourceSettings.getInstance().getConfigFile());
    }

    private static Environment initEnv() {
        String envFileName = GlobalTestConfigurationProvider.provide().getEnvironments().getEnvironment().stream()
                .filter(Env::isEnable)
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException("No enabled environment was found"))
                .getFile();
        File envFile = FileSearcher.searchFileFromDir(TestResourceSettings.getInstance().getEnvironmentFolder(),
                envFileName);
        return XMLParsers.forEnvironmentConfiguration().process(envFile);
    }
}
