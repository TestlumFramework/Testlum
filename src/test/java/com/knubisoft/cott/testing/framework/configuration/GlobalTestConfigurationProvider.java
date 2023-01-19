package com.knubisoft.cott.testing.framework.configuration;

import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.validator.GlobalTestConfigValidator;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.ConfigFiles;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.global_config.Ui;
import com.knubisoft.cott.testing.model.global_config.Web;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DISABLED_IN_CONFIG;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();
    private static final Integrations INTEGRATIONS = initIntegrations();
    private static final Ui UI = initUi();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static Integrations getIntegrations() {
        return INTEGRATIONS;
    }

    public static Ui provideUi() {
        return UI;
    }

    public static List<AbstractBrowser> getBrowsers() {
        if (Objects.nonNull(getWebSettings())) {
            return getWebSettings().getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari();
        }
        return Collections.emptyList();
    }

    public static List<NativeDevice> getNativeDevices() {
        if (Objects.nonNull(getNativeSettings())) {
            return getNativeSettings().getDevices().getDevice();
        }
        return Collections.emptyList();
    }

    public static List<MobilebrowserDevice> getMobilebrowserDevices() {
        if (Objects.nonNull(getMobilebrowserSettings())) {
            return getMobilebrowserSettings().getDevices().getDevice();
        }
        return Collections.emptyList();
    }

    public static Web getWebSettings() {
        return GlobalTestConfigurationProvider.provideUi().getWeb();
    }

    public static Mobilebrowser getMobilebrowserSettings() {
        return GlobalTestConfigurationProvider.provideUi().getMobilebrowser();
    }

    public static Native getNativeSettings() {
        return GlobalTestConfigurationProvider.provideUi().getNative();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration()
                .process(TestResourceSettings.getInstance().getConfigFile(), new GlobalTestConfigValidator());
    }

    private static Integrations initIntegrations() {
        ConfigFiles configFile = GlobalTestConfigurationProvider.provide().getIntegrations();
        if (configFile.isEnable()) {
            return XMLParsers.forIntegrations().process(FileSearcher.getFileFromConfigFolder(configFile.getFile()));
        }
        log.warn(DISABLED_IN_CONFIG, "Integrations", "integrations");
        return new Integrations();
    }

    private static Ui initUi() {
        ConfigFiles configFile = GlobalTestConfigurationProvider.provide().getUi();
        if (configFile.isEnable()) {
            return XMLParsers.forUi().process(FileSearcher.getFileFromConfigFolder(configFile.getFile()));
        }
        log.warn(DISABLED_IN_CONFIG, "UI", "ui");
        return new Ui();
    }


}
