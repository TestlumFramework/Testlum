package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.InjectionService;
import com.knubisoft.testlum.testing.framework.validator.IntegrationsValidator;
import com.knubisoft.testlum.testing.framework.validator.UiConfigValidator;
import com.knubisoft.testlum.testing.framework.vault.VaultService;
import com.knubisoft.testlum.testing.framework.xml.XMLParser;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GlobalTestConfigurationProviderTest {

    private TestResourceSettings testResourceSettings;
    private FileSearcher fileSearcher;
    private XMLParsers xmlParsers;
    private UiConfigValidator uiConfigValidator;
    private IntegrationsValidator integrationsValidator;
    private InjectionService injectionService;
    private GlobalTestConfigurationProvider provider;

    @BeforeEach
    void setUp() {
        testResourceSettings = mock(TestResourceSettings.class);
        fileSearcher = mock(FileSearcher.class);
        xmlParsers = mock(XMLParsers.class);
        uiConfigValidator = mock(UiConfigValidator.class);
        integrationsValidator = mock(IntegrationsValidator.class);
        injectionService = mock(InjectionService.class);
        provider = new GlobalTestConfigurationProvider(
                testResourceSettings, fileSearcher, xmlParsers,
                uiConfigValidator, integrationsValidator, injectionService);
    }

    @Nested
    class GlobalTestConfigurationBean {
        @Test
        @SuppressWarnings("unchecked")
        void returnsConfigFromXmlParser() {
            File configFile = new File("/test/global.xml");
            when(testResourceSettings.getConfigFile()).thenReturn(configFile);
            XMLParser<GlobalTestConfiguration> parser = mock(XMLParser.class);
            when(xmlParsers.forGlobalTestConfiguration()).thenReturn(parser);
            GlobalTestConfiguration expected = new GlobalTestConfiguration();
            when(parser.process(configFile)).thenReturn(expected);

            GlobalTestConfiguration result = provider.globalTestConfiguration();
            assertEquals(expected, result);
        }
    }

    @Nested
    class VaultServiceBean {
        @Test
        void returnsNullWhenVaultNotConfigured() {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setVault(null);

            VaultService result = provider.vaultService(config);
            assertNull(result);
        }
    }

    @Nested
    class GetEnvironmentsBean {
        @Test
        void filtersOnlyEnabledEnvironments() {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            Environments envs = new Environments();
            Environment enabled = createEnvironment("dev", true);
            Environment disabled = createEnvironment("staging", false);
            envs.getEnv().add(enabled);
            envs.getEnv().add(disabled);
            config.setEnvironments(envs);

            List<Environment> result = provider.getEnvironments(config);
            assertEquals(1, result.size());
            assertEquals("dev", result.get(0).getFolder());
        }

        @Test
        void returnsEmptyWhenNoEnabledEnvironments() {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            Environments envs = new Environments();
            envs.getEnv().add(createEnvironment("dev", false));
            config.setEnvironments(envs);

            List<Environment> result = provider.getEnvironments(config);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetIntegrationsBean {
        @Test
        @SuppressWarnings("unchecked")
        void collectsIntegrationsForEachEnvironment() {
            Environment env = createEnvironment("dev", true);
            List<Environment> environments = List.of(env);

            Integrations integrations = new Integrations();
            XMLParser<Integrations> parser = mock(XMLParser.class);
            when(xmlParsers.forIntegrations()).thenReturn(parser);
            when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                    .thenReturn(Optional.of(new File("/test/integration.xml")));
            when(parser.process(any(File.class))).thenReturn(integrations);
            when(injectionService.injectFromSystem(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(integrationsValidator).validate(anyMap());

            EnvToIntegrationMap result = provider.getIntegrations(environments, Optional.empty());
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.containsKey("dev"));
        }

        @Test
        void returnsEmptyIntegrationsWhenConfigFileNotFound() {
            Environment env = createEnvironment("dev", true);
            List<Environment> environments = List.of(env);

            when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            doNothing().when(integrationsValidator).validate(anyMap());

            EnvToIntegrationMap result = provider.getIntegrations(environments, Optional.empty());
            assertNotNull(result);
            assertNotNull(result.get("dev"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void injectsFromVaultWhenPresent() {
            Environment env = createEnvironment("dev", true);
            List<Environment> environments = List.of(env);

            Integrations integrations = new Integrations();
            XMLParser<Integrations> parser = mock(XMLParser.class);
            VaultService vaultService = mock(VaultService.class);
            when(xmlParsers.forIntegrations()).thenReturn(parser);
            when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                    .thenReturn(Optional.of(new File("/test/integration.xml")));
            when(parser.process(any(File.class))).thenReturn(integrations);
            when(injectionService.injectFromVault(any(VaultService.class), any()))
                    .thenAnswer(inv -> inv.getArgument(1));
            when(injectionService.injectFromSystem(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(integrationsValidator).validate(anyMap());

            EnvToIntegrationMap result = provider.getIntegrations(environments, Optional.of(vaultService));
            assertNotNull(result);
            verify(injectionService).injectFromVault(any(VaultService.class), any());
        }
    }

    @Nested
    class GetUiConfigsBean {
        @Test
        @SuppressWarnings("unchecked")
        void collectsUiConfigsForEachEnvironment() {
            Environment env = createEnvironment("dev", true);
            List<Environment> environments = List.of(env);

            UiConfig uiConfig = new UiConfig();
            XMLParser<UiConfig> parser = mock(XMLParser.class);
            when(xmlParsers.forUiConfig()).thenReturn(parser);
            when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                    .thenReturn(Optional.of(new File("/test/ui.xml")));
            when(parser.process(any(File.class))).thenReturn(uiConfig);
            when(injectionService.injectFromSystem(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(uiConfigValidator).validate(anyMap());

            UIConfiguration result = provider.getUiConfigs(environments, Optional.empty());
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.containsKey("dev"));
        }

        @Test
        void returnsDefaultUiConfigWhenFileNotFound() {
            Environment env = createEnvironment("dev", true);
            List<Environment> environments = List.of(env);

            when(fileSearcher.searchFileFromEnvFolder(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            doNothing().when(uiConfigValidator).validate(anyMap());

            UIConfiguration result = provider.getUiConfigs(environments, Optional.empty());
            assertNotNull(result);
            assertNotNull(result.get("dev"));
        }
    }

    @Nested
    class GetDefaultIntegrationsBean {
        @Test
        void returnsFirstEnvironmentIntegrations() {
            Integrations devIntegrations = new Integrations();
            EnvToIntegrationMap map = new EnvToIntegrationMap(
                    java.util.Map.of("dev", devIntegrations));
            List<Environment> environments = List.of(createEnvironment("dev", true));

            Integrations result = provider.getDefaultIntegrations(map, environments);
            assertEquals(devIntegrations, result);
        }

        @Test
        void throwsWhenNoEnvironments() {
            EnvToIntegrationMap map = new EnvToIntegrationMap(java.util.Map.of());
            List<Environment> environments = Collections.emptyList();

            assertThrows(DefaultFrameworkException.class,
                    () -> provider.getDefaultIntegrations(map, environments));
        }
    }

    @Nested
    class GetDefaultUiConfigsBean {
        @Test
        void returnsFirstEnvironmentUiConfig() {
            UiConfig devConfig = new UiConfig();
            UIConfiguration uiConfig = new UIConfiguration(java.util.Map.of("dev", devConfig));
            List<Environment> environments = List.of(createEnvironment("dev", true));

            UiConfig result = provider.getDefaultUiConfigs(uiConfig, environments);
            assertEquals(devConfig, result);
        }

        @Test
        void throwsWhenNoEnvironments() {
            UIConfiguration uiConfig = new UIConfiguration(java.util.Map.of());
            List<Environment> environments = Collections.emptyList();

            assertThrows(DefaultFrameworkException.class,
                    () -> provider.getDefaultUiConfigs(uiConfig, environments));
        }
    }

    private Environment createEnvironment(final String folder, final boolean enabled) {
        Environment env = new Environment();
        env.setFolder(folder);
        env.setEnabled(enabled);
        return env;
    }
}
