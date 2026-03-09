package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.validator.IntegrationsValidator;
import com.knubisoft.testlum.testing.framework.validator.UiConfigValidator;
import com.knubisoft.testlum.testing.framework.vault.VaultService;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalTestConfigurationProvider {

    private final TestResourceSettings testResourceSettings;
    private final FileSearcher fileSearcher;
    private final XMLParsers xmlParsers;
    private final UiConfigValidator validator;
    private final IntegrationsValidator integrationsValidator;
    private final InjectionUtil injectionUtil;

    @Bean
    public GlobalTestConfiguration globalTestConfiguration() {
        return xmlParsers.forGlobalTestConfiguration().process(testResourceSettings.getConfigFile());
    }

    @Bean
    public VaultService vaultService(final GlobalTestConfiguration globalTestConfiguration) {
        Vault vault = globalTestConfiguration.getVault();
        return vault == null ? null : new VaultService(globalTestConfiguration);
    }

    @Bean
    public List<Environment> getEnvironments(final GlobalTestConfiguration globalTestConfiguration) {
        return filterEnabledEnvironments(globalTestConfiguration);
    }

    @Bean
    public EnvToIntegrationMap getIntegrations(final List<Environment> environments,
                                               final Optional<VaultService> vaultService) {
        return collectIntegrations(environments, vaultService);
    }

    public static class EnvToIntegrationMap extends HashMap<String, Integrations> {
        public EnvToIntegrationMap(final Map<? extends String, ? extends Integrations> m) {
            super(m);
        }
    }

    public static class UIConfiguration extends HashMap<String, UiConfig> {
        public UIConfiguration(final Map<? extends String, ? extends UiConfig> m) {
            super(m);
        }
    }

    @Bean("uiConfig")
    public UIConfiguration getUiConfigs(final List<Environment> environments,
                                        final Optional<VaultService> vaultService) {
        return collectUiConfigs(environments, vaultService);
    }

    @Bean
    public Integrations getDefaultIntegrations(final EnvToIntegrationMap integrations,
                                               final List<Environment> environments) {
        return integrations.get(getDefaultEnabledEnvironment(environments));
    }

    @Bean
    public UiConfig getDefaultUiConfigs(@Qualifier("uiConfig") final UIConfiguration uiConfigs,
                                        final List<Environment> environments) {
        return uiConfigs.get(getDefaultEnabledEnvironment(environments));
    }

    private List<Environment> filterEnabledEnvironments(final GlobalTestConfiguration globalTestConfiguration) {
        return globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled).toList();
    }

    private EnvToIntegrationMap collectIntegrations(final List<Environment> environments,
                                                    final Optional<VaultService> vaultService) {
        Map<String, Integrations> integrationsMap = environments.stream()
                .collect(Collectors.toMap(Environment::getFolder, e -> initIntegration(e, vaultService)));
        integrationsValidator.validate(integrationsMap);
        return new EnvToIntegrationMap(integrationsMap);
    }

    private Integrations initIntegration(final Environment env, final Optional<VaultService> vaultService) {
        return fileSearcher
                .searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.INTEGRATION_CONFIG_FILENAME)
                .map(configFile -> xmlParsers.forIntegrations().process(configFile))
                .map(e -> injectFromVaultIfPresent(vaultService, e))
                .map(injectionUtil::injectFromSystem)
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, Integrations.class.getSimpleName());
                    return new Integrations();
                });
    }

    private UIConfiguration collectUiConfigs(final List<Environment> environments,
                                             final Optional<VaultService> vaultService) {
        Map<String, UiConfig> uiConfigMap = environments.stream()
                .collect(Collectors.toMap(Environment::getFolder, env -> initUiConfig(env, vaultService)));
        validator.validate(uiConfigMap);
        return new UIConfiguration(uiConfigMap);
    }

    private UiConfig initUiConfig(final Environment env,
                                  final Optional<VaultService> vaultService) {
        return fileSearcher.searchFileFromEnvFolder(env.getFolder(), TestResourceSettings.UI_CONFIG_FILENAME)
                .map(configFile -> xmlParsers.forUiConfig().process(configFile))
                .map(e -> injectFromVaultIfPresent(vaultService, e))
                .map(injectionUtil::injectFromSystem)
                .orElseGet(() -> {
                    log.warn(LogMessage.DISABLED_CONFIGURATION, UiConfig.class.getSimpleName());
                    return new UiConfig();
                });
    }

    private <T> T injectFromVaultIfPresent(final Optional<VaultService> vaultService, final T t) {
        return vaultService.map(service -> injectionUtil.injectFromVault(service, t)).orElse(t);
    }

    private String getDefaultEnabledEnvironment(final List<Environment> environments) {
        if (environments.isEmpty()) {
            throw new DefaultFrameworkException(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
        }
        return environments.get(0).getFolder();
    }
}