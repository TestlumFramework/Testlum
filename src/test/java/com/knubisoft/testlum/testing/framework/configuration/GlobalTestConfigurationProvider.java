package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.validator.GlobalTestConfigValidator;
import com.knubisoft.testlum.testing.framework.validator.IntegrationsValidator;
import com.knubisoft.testlum.testing.framework.validator.UiConfigValidator;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();

    private static final List<Environment> ENVIRONMENTS = filterEnabledEnvironments();
    private static final Map<String, Integrations> INTEGRATIONS = collectIntegrations();
    private static final Map<String, UiConfig> UI_CONFIGS = collectUiConfigs();
    private static final Integrations DEFAULT_INTEGRATIONS = defaultIntegrations();
    private static final UiConfig DEFAULT_UI_CONFIGS = defaultUiConfigs();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    public static List<Environment> getEnabledEnvironments() {
        return ENVIRONMENTS;
    }

    public static Map<String, Integrations> getIntegrations() {
        return INTEGRATIONS;
    }

    public static Map<String, UiConfig> getUiConfigs() {
        return UI_CONFIGS;
    }

    public static Integrations getDefaultIntegrations() {
        return DEFAULT_INTEGRATIONS;
    }

    public static UiConfig getDefaultUiConfigs() {
        return DEFAULT_UI_CONFIGS;
    }

    public static Web getWebSettings(final String env) {
        return getUiConfigs().get(env).getWeb();
    }

    public static Mobilebrowser getMobilebrowserSettings(final String env) {
        return getUiConfigs().get(env).getMobilebrowser();
    }

    public static Native getNativeSettings(final String env) {
        return getUiConfigs().get(env).getNative();
    }

    private static GlobalTestConfiguration init() {
        return XMLParsers.forGlobalTestConfiguration()
                .process(TestResourceSettings.getInstance().getConfigFile(), new GlobalTestConfigValidator());
    }

    private static List<Environment> filterEnabledEnvironments() {
        return provide().getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled).collect(Collectors.toList());
    }

    private static Map<String, Integrations> collectIntegrations() {
        Map<String, Integrations> integrationsMap = getEnabledEnvironments().stream()
                .collect(Collectors.toMap(Environment::getFolder, GlobalTestConfigurationProvider::initIntegration));
        new IntegrationsValidator().validate(integrationsMap);
        return integrationsMap;
    }

    private static Integrations initIntegration(final Environment env) {
        return FileSearcher.searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                .map(configFile -> XMLParsers.forIntegrations().process(configFile))
                .map(t -> {
                    if (Objects.nonNull(GlobalTestConfigurationProvider.provide().getVault())) {
                        return InjectionUtil.injectFromVault(t);
                    } else {
                    return t;
                    }
                })
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, Integrations.class.getSimpleName());
                    return new Integrations();
                });
    }

    private static Map<String, UiConfig> collectUiConfigs() {
        Map<String, UiConfig> uiConfigMap = getEnabledEnvironments().stream()
                .collect(Collectors.toMap(Environment::getFolder, GlobalTestConfigurationProvider::initUiConfig));
        new UiConfigValidator().validate(uiConfigMap);
        return uiConfigMap;
    }

    private static UiConfig initUiConfig(final Environment env) {
        return FileSearcher.searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.UI_CONFIG_FILENAME)
                .map(configFile -> XMLParsers.forUiConfig().process(configFile))
                .map(t -> {
                    if (Objects.nonNull(GlobalTestConfigurationProvider.provide().getVault())) {
                    return InjectionUtil.injectFromVault(t);
                    } else {
                    return t;
                    }
                })
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, UiConfig.class.getSimpleName());
                    return new UiConfig();
                });
    }

    private static Integrations defaultIntegrations() {
        return getIntegrations().get(getDefaultEnabledEnvironment());
    }

    private static UiConfig defaultUiConfigs() {
        return getUiConfigs().get(getDefaultEnabledEnvironment());
    }

    private static String getDefaultEnabledEnvironment() {
        if (getEnabledEnvironments().isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
        }
        return getEnabledEnvironments().get(0).getFolder();
    }
}
