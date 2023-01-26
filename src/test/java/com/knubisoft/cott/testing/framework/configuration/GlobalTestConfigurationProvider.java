package com.knubisoft.cott.testing.framework.configuration;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.parser.XMLParsers;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.validator.GlobalTestConfigValidator;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.ConfigFiles;
import com.knubisoft.cott.testing.model.global_config.Environment;
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
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DISABLED_IN_CONFIG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INTEGRATIONS_FILE_NOT_SPECIFIED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UI_FILE_NOT_SPECIFIED;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();
    private static final Map<String, Integrations> INTEGRATIONS = collectIntegrations();
    private static final Map<String, Ui> UI = collectUiConfigs();
    private static final List<Environment> ENVIRONMENTS = provide().getEnvironments().getEnvironment().stream()
            .filter(Environment::isEnable).collect(Collectors.toList());
    private static final Integrations DEFAULT_INTEGRATION = initIntegration(getEnabledEnvironments().get(0).getFolder(),
            getEnabledEnvironments().get(0).getIntegrationsConfigurations());

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static Map<String, Integrations> getIntegrations() {
        return INTEGRATIONS;
    }

    public static Map<String, Ui> provideUi() {
        return UI;
    }

    public static List<Environment> getEnabledEnvironments() {
        return ENVIRONMENTS;
    }

    public static Integrations getDefaultIntegration() {
        return DEFAULT_INTEGRATION;
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

    public static boolean isWebParallel() {
        return getWebSettings().getBrowserSettings().isParallelExecution();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration()
                .process(TestResourceSettings.getInstance().getConfigFile(), new GlobalTestConfigValidator());
    }

    private static Map<String, Integrations> collectIntegrations() {
        Map<String, Integrations> integrationsMap = new HashMap<>();
        getEnabledEnvironments().forEach(env -> integrationsMap.put(env.getFolder(),
                initIntegration(env.getFolder(), env.getIntegrationsConfigurations())));
        return integrationsMap;
    }

    private static Map<String, Ui> collectUiConfigs() {
        Map<String, Ui> integrationsMap = new HashMap<>();
        getEnabledEnvironments().forEach(env -> integrationsMap.put(env.getFolder(),
                initUi(env.getFolder(), env.getUiConfigurations())));
        return integrationsMap;
    }

    private static Integrations initIntegration(final String folder, final ConfigFiles configFile) {
        if (configFile.isEnable()) {
            if (StringUtils.isBlank(configFile.getFile())) {
                throw new DefaultFrameworkException(INTEGRATIONS_FILE_NOT_SPECIFIED);
            }
            return XMLParsers.forIntegrations()
                    .process(FileSearcher.searchFileFromEnvFolder(folder, configFile.getFile()));
        }
        log.warn(DISABLED_IN_CONFIG, "Integrations", "integrations");
        return new Integrations();
    }

    private static Ui initUi(final String folder, final ConfigFiles configFile) {
        if (configFile.isEnable()) {
            if (StringUtils.isBlank(configFile.getFile())) {
                throw new DefaultFrameworkException(UI_FILE_NOT_SPECIFIED);
            }
            return XMLParsers.forUi().process(FileSearcher.searchFileFromEnvFolder(folder, configFile.getFile()));
        }
        log.warn(DISABLED_IN_CONFIG, "UI", "ui");
        return new Ui();
    }


}
