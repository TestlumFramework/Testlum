package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.validator.IntegrationsValidator;
import com.knubisoft.testlum.testing.framework.validator.UiConfigValidator;
import com.knubisoft.testlum.testing.framework.vault.VaultService;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class GlobalTestConfigurationProvider {

    private static volatile GlobalTestConfigurationProvider instance;

    private final GlobalTestConfiguration globalTestConfiguration;
    private final List<Environment> environments;
    private final Map<String, Integrations> integrations;
    private final Map<String, UiConfig> uiConfigs;
    private final Integrations defaultIntegrations;
    private final UiConfig defaultUiConfigs;
    private final Optional<VaultService> vaultService;

    private GlobalTestConfigurationProvider() {
        this.globalTestConfiguration = handle(this::init,
                "Unable to retrieve global test configuration");
        this.vaultService = handle(() -> initVault(globalTestConfiguration),
                "Unable to init vault");
        this.environments = handle(() -> filterEnabledEnvironments(globalTestConfiguration),
                "Unable to retrieve environments");
        this.integrations = handle(() -> collectIntegrations(environments),
                "Unable to retrieve integrations");
        this.uiConfigs = handle(() -> collectUiConfigs(environments),
                "Unable to retrieve ui configs");
        this.defaultIntegrations = handle(() -> defaultIntegrations(integrations),
                "Unable to retrieve default integrations");
        this.defaultUiConfigs = handle(() -> defaultUiConfigs(uiConfigs),
                "Unable to retrieve default ui configs");
    }

    public static GlobalTestConfigurationProvider get() {
        GlobalTestConfigurationProvider provider = instance;
        if (provider == null) {
            synchronized (GlobalTestConfigurationProvider.class) {
                provider = instance;
                if (provider == null) {
                    instance = new GlobalTestConfigurationProvider();
                    provider = instance;
                }
            }
        }
        return provider;
    }

    private Optional<VaultService> initVault(final GlobalTestConfiguration globalTestConfiguration) {
        Vault vault = globalTestConfiguration.getVault();
        if (vault != null) {
            return Optional.of(new VaultService(globalTestConfiguration));
        } else {
            return Optional.empty();
        }
    }

    private <T> T handle(final Supplier<T> s, final String message) {
        try {
            return s.get();
        } catch (Exception e) {
            log.error(message, e);
            throw new DefaultFrameworkException(e);
        }
    }

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
                .process(TestResourceSettings.getInstance().getConfigFile());
    }

    private List<Environment> filterEnabledEnvironments(final GlobalTestConfiguration globalTestConfiguration) {
        return globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled).collect(Collectors.toList());
    }

    private Map<String, Integrations> collectIntegrations(final List<Environment> environments) {
        Map<String, Integrations> integrationsMap = environments.stream()
                .collect(Collectors.toMap(Environment::getFolder, this::initIntegration));
        new IntegrationsValidator().validate(integrationsMap);
        return integrationsMap;
    }

    private Integrations initIntegration(final Environment env) {
        return FileSearcher
                .searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                .map(configFile -> XMLParsers.forIntegrations().process(configFile))
                .map(this::injectFromVaultIfPresent)
                .map(InjectionUtil::injectFromSystem)
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, Integrations.class.getSimpleName());
                    return new Integrations();
                });
    }

    private Map<String, UiConfig> collectUiConfigs(final List<Environment> environments) {
        Map<String, UiConfig> uiConfigMap = environments.stream()
                .collect(Collectors.toMap(Environment::getFolder, this::initUiConfig));
        new UiConfigValidator().validate(uiConfigMap);
        return uiConfigMap;
    }

    private UiConfig initUiConfig(final Environment env) {
        return FileSearcher.searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.UI_CONFIG_FILENAME)
                .map(configFile -> XMLParsers.forUiConfig().process(configFile))
                .map(this::injectFromVaultIfPresent)
                .map(InjectionUtil::injectFromSystem)
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, UiConfig.class.getSimpleName());
                    return new UiConfig();
                });
    }

    private <T> T injectFromVaultIfPresent(final T t) {
        return vaultService.map(service -> InjectionUtil.injectFromVault(service, t)).orElse(t);
    }

    private Integrations defaultIntegrations(final Map<String, Integrations> integrations) {
        return integrations.get(getDefaultEnabledEnvironment());
    }

    private UiConfig defaultUiConfigs(final Map<String, UiConfig> uiConfigs) {
        return uiConfigs.get(getDefaultEnabledEnvironment());
    }

    private String getDefaultEnabledEnvironment() {
        if (getEnabledEnvironments().isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
        }
        return getEnabledEnvironments().get(0).getFolder();
    }
}