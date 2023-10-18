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
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ConfigProviderImpl implements ConfigProvider {

    @Override
    public GlobalTestConfiguration provide() {
        return GlobalTestConfigurationProvider.provide();
    }

    @Override
    public List<Environment> getEnabledEnvironments() {
        return GlobalTestConfigurationProvider.getEnabledEnvironments();
    }

    @Override
    public Map<String, Integrations> getIntegrations() {
        return GlobalTestConfigurationProvider.getIntegrations();
    }

    @Override
    public Map<String, UiConfig> getUiConfigs() {
        return GlobalTestConfigurationProvider.getUiConfigs();
    }

    @Override
    public Integrations getDefaultIntegrations() {
        return GlobalTestConfigurationProvider.getDefaultIntegrations();
    }

    @Override
    public UiConfig getDefaultUiConfigs() {
        return GlobalTestConfigurationProvider.getDefaultUiConfigs();
    }

    @Override
    public Web getWebSettings(final String env) {
        return GlobalTestConfigurationProvider.getWebSettings(env);
    }

    @Override
    public Mobilebrowser getMobilebrowserSettings(final String env) {
        return GlobalTestConfigurationProvider.getMobilebrowserSettings(env);
    }

    @Override
    public Native getNativeSettings(final String env) {
        return GlobalTestConfigurationProvider.getNativeSettings(env);
    }

    @Slf4j
    @UtilityClass
    public static class GlobalTestConfigurationProvider {

        private final GlobalTestConfiguration globalTestConfiguration = init();
        private final List<Environment> environments = filterEnabledEnvironments();
        @Getter
        private final Map<String, Integrations> integrations = collectIntegrations();
        @Getter
        private final Map<String, UiConfig> uiConfigs = collectUiConfigs();
        @Getter
        private final Integrations defaultIntegrations = defaultIntegrations();
        @Getter
        private final UiConfig defaultUiConfigs = defaultUiConfigs();

        public GlobalTestConfiguration provide() {
            return globalTestConfiguration;
        }

        public List<Environment> getEnabledEnvironments() {
            return environments;
        }

        public Web getWebSettings(final String env) {
            return getUiConfigs().get(env).getWeb();
        }

        public Mobilebrowser getMobilebrowserSettings(final String env) {
            return getUiConfigs().get(env).getMobilebrowser();
        }

        public Native getNativeSettings(final String env) {
            return getUiConfigs().get(env).getNative();
        }

        private GlobalTestConfiguration init() {
            return XMLParsers.forGlobalTestConfiguration()
                    .process(TestResourceSettings.getInstance().getConfigFile(), new GlobalTestConfigValidator());
        }

        private List<Environment> filterEnabledEnvironments() {
            return provide().getEnvironments().getEnv().stream()
                    .filter(Environment::isEnabled).collect(Collectors.toList());
        }

        private Map<String, Integrations> collectIntegrations() {
            Map<String, Integrations> integrationsMap = getEnabledEnvironments().stream()
                    .collect(Collectors.toMap(Environment::getFolder,
                            GlobalTestConfigurationProvider::initIntegration));
            new IntegrationsValidator().validate(integrationsMap);
            return integrationsMap;
        }

        private Integrations initIntegration(final Environment env) {
            return FileSearcher
                    .searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                    .map(configFile -> XMLParsers.forIntegrations().process(configFile))
                    .map(GlobalTestConfigurationProvider::checkIfVaultPresent)
                    .map(InjectionUtil::injectFromSystem)
                    .orElseGet(() -> {
                        log.warn(LogMessage.DISABLED_CONFIGURATION, Integrations.class.getSimpleName());
                        return new Integrations();
                    });
        }

        private Map<String, UiConfig> collectUiConfigs() {
            Map<String, UiConfig> uiConfigMap = getEnabledEnvironments().stream()
                    .collect(Collectors.toMap(Environment::getFolder, GlobalTestConfigurationProvider::initUiConfig));
            new UiConfigValidator().validate(uiConfigMap);
            return uiConfigMap;
        }

        private UiConfig initUiConfig(final Environment env) {
            return FileSearcher.searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.UI_CONFIG_FILENAME)
                    .map(configFile -> XMLParsers.forUiConfig().process(configFile))
                    .map(GlobalTestConfigurationProvider::checkIfVaultPresent)
                    .map(InjectionUtil::injectFromSystem)
                    .orElseGet(() -> {
                        log.warn(LogMessage.DISABLED_CONFIGURATION, UiConfig.class.getSimpleName());
                        return new UiConfig();
                    });
        }

        private <T> T checkIfVaultPresent(final T t) {
            if (Objects.nonNull(provide().getVault())) {
                return InjectionUtil.injectFromVault(t);
            } else {
                return t;
            }
        }

        private Integrations defaultIntegrations() {
            return getIntegrations().get(getDefaultEnabledEnvironment());
        }

        private UiConfig defaultUiConfigs() {
            return getUiConfigs().get(getDefaultEnabledEnvironment());
        }

        private String getDefaultEnabledEnvironment() {
            if (getEnabledEnvironments().isEmpty()) {
                throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
            }
            return getEnabledEnvironments().get(0).getFolder();
        }
    }
}
