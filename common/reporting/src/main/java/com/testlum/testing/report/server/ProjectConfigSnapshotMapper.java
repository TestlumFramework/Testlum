package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.*;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.UIConfiguration;
import com.testlum.testing.model.global_config.*;
import com.testlum.testing.model.global_config.DelayBetweenScenarioRuns;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class ProjectConfigSnapshotMapper {

    private final GlobalTestConfiguration globalTestConfiguration;
    private final EnvToIntegrationMap envToIntegrationMap;
    private final UIConfiguration uiConfiguration;
    private final IntegrationConfigMapper integrationConfigMapper;
    private final LocatorSnapshotMapper locatorSnapshotMapper;
    private final UiConfigMapper uiConfigMapper;

    public ProjectConfigSnapshotMapper(final GlobalTestConfiguration globalTestConfiguration,
                                       final EnvToIntegrationMap envToIntegrationMap,
                                       @Qualifier("uiConfig") final UIConfiguration uiConfiguration,
                                       final IntegrationConfigMapper integrationConfigMapper,
                                       final LocatorSnapshotMapper locatorSnapshotMapper,
                                       final UiConfigMapper uiConfigMapper) {
        this.globalTestConfiguration = globalTestConfiguration;
        this.envToIntegrationMap = envToIntegrationMap;
        this.uiConfiguration = uiConfiguration;
        this.integrationConfigMapper = integrationConfigMapper;
        this.locatorSnapshotMapper = locatorSnapshotMapper;
        this.uiConfigMapper = uiConfigMapper;
    }

    public ProjectConfigSnapshot map() {
        return ProjectConfigSnapshot.builder()
                .projectName(globalTestConfiguration.getReport().getProjectName())
                .globalConfig(globalConfig())
                .environments(environments())
                .pages(locatorSnapshotMapper.pages())
                .components(locatorSnapshotMapper.components())
                .build();
    }

    private GlobalConfig globalConfig() {
        GlobalTestConfiguration config = globalTestConfiguration;
        return GlobalConfig.builder()
                .parallelExecution(Boolean.TRUE.equals(config.isParallelExecution()))
                .stopScenarioOnFailure(config.isStopScenarioOnFailure())
                .stopIfInvalidScenario(config.isStopIfInvalidScenario())
                .delayBetweenScenarioRuns(delay(config.getDelayBetweenScenarioRuns()))
                .runScenariosByTag(runByTag(config.getRunScenariosByTag()))
                .report(report(config.getReport()))
                .vault(vault(config.getVault()))
                .build();
    }

    private com.testlum.reporting.sdk.model.config.DelayBetweenScenarioRuns delay(
            final DelayBetweenScenarioRuns delay) {
        if (delay == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.DelayBetweenScenarioRuns.builder()
                .seconds(delay.getSeconds())
                .enabled(delay.isEnabled())
                .build();
    }

    private com.testlum.reporting.sdk.model.config.RunScenariosByTag runByTag(final RunScenariosByTag byTag) {
        if (byTag == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.RunScenariosByTag.builder()
                .enabled(byTag.isEnabled())
                .tags(byTag.getTag().stream().map(this::tag).toList())
                .build();
    }

    private TagConfig tag(final TagValue tag) {
        return TagConfig.builder()
                .name(tag.getName())
                .enabled(tag.isEnabled())
                .build();
    }

    private ReportConfig report(final Report report) {
        return ReportConfig.builder()
                .projectName(report.getProjectName())
                .onlyFailedScenarios(report.isOnlyFailedScenarios())
                .htmlReportEnabled(report.getHtmlReport() != null && report.getHtmlReport().isEnabled())
                .build();
    }

    private VaultConfig vault(final Vault vault) {
        if (vault == null) {
            return null;
        }
        return VaultConfig.builder()
                .host(vault.getHost())
                .port(vault.getPort())
                .scheme(vault.getScheme())
                .token(vault.getToken())
                .build();
    }

    private List<EnvironmentConfig> environments() {
        if (globalTestConfiguration.getEnvironments() == null) {
            return List.of();
        }
        return globalTestConfiguration.getEnvironments().getEnv().stream().map(this::environment).toList();
    }

    private EnvironmentConfig environment(final Environment environment) {
        String folder = environment.getFolder();
        return EnvironmentConfig.builder()
                .folder(folder)
                .enabled(environment.isEnabled())
                .threads(environment.getThreads())
                .integrations(integrationConfigMapper.map(envToIntegrationMap.get(folder)))
                .ui(uiConfigMapper.map(uiConfiguration.get(folder)))
                .build();
    }
}
