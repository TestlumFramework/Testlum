package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.EnvironmentConfig;
import com.testlum.reporting.sdk.model.config.ProjectConfigSnapshot;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.UIConfiguration;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProjectConfigSnapshotMapper} verifying the full project config snapshot.
 */
class ProjectConfigSnapshotMapperTest {

    private final GlobalTestConfiguration config = new GlobalTestConfiguration();
    private final LocatorSnapshotMapper locatorSnapshotMapper = mock(LocatorSnapshotMapper.class);
    private ProjectConfigSnapshotMapper mapper;

    private static Report report() {
        Report report = new Report();
        report.setProjectName("demo");
        report.setOnlyFailedScenarios(true);
        return report;
    }

    private static Vault vault() {
        Vault vault = new Vault();
        vault.setHost("vault.local");
        vault.setToken("s.token");
        return vault;
    }

    private static Environments environments() {
        Environments environments = new Environments();
        environments.getEnv().add(environment("staging", true));
        environments.getEnv().add(environment("prod", false));
        return environments;
    }

    private static Environment environment(final String folder, final boolean enabled) {
        Environment environment = new Environment();
        environment.setFolder(folder);
        environment.setEnabled(enabled);
        return environment;
    }

    private static UiConfig uiConfig() {
        Web web = new Web();
        web.setEnabled(true);
        web.setBaseUrl("https://app.test");
        UiConfig uiConfig = new UiConfig();
        uiConfig.setWeb(web);
        return uiConfig;
    }

    @BeforeEach
    void setUp() {
        config.setReport(report());
        config.setVault(vault());
        config.setEnvironments(environments());
        when(locatorSnapshotMapper.pages()).thenReturn(List.of());
        when(locatorSnapshotMapper.components()).thenReturn(List.of());
        JacksonService jacksonService = new JacksonService();
        mapper = new ProjectConfigSnapshotMapper(config,
                new EnvToIntegrationMap(Map.of("staging", new Integrations())),
                new UIConfiguration(Map.of("staging", uiConfig())),
                new IntegrationConfigMapper(jacksonService), locatorSnapshotMapper, jacksonService);
    }

    @Test
    void mapsGlobalConfigWithSecretsAsIs() {
        ProjectConfigSnapshot snapshot = mapper.map();

        assertEquals("demo", snapshot.getProjectName());
        assertTrue(snapshot.getGlobalConfig().getReport().isOnlyFailedScenarios());
        assertEquals("s.token", snapshot.getGlobalConfig().getVault().getToken());
    }

    @Test
    void mapsEveryEnvironmentIncludingDisabledOnes() {
        List<EnvironmentConfig> environments = mapper.map().getEnvironments();

        assertEquals(2, environments.size());
        EnvironmentConfig staging = environments.get(0);
        assertEquals("staging", staging.getFolder());
        assertEquals("https://app.test", staging.getUi().getWeb().getBaseUrl());
        assertEquals("https://app.test", staging.getUi().getWeb().getDetails().get("baseUrl"));
        EnvironmentConfig disabled = environments.get(1);
        assertTrue(disabled.getIntegrations().isEmpty());
        assertNull(disabled.getUi());
    }
}
