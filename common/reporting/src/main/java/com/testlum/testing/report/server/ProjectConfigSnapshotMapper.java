package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.config.*;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.UIConfiguration;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.model.global_config.*;
import com.testlum.testing.model.global_config.BrowserStackLogin;
import com.testlum.testing.model.global_config.DelayBetweenScenarioRuns;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.UiConfig;
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
    private final JacksonService jacksonService;

    public ProjectConfigSnapshotMapper(final GlobalTestConfiguration globalTestConfiguration,
                                       final EnvToIntegrationMap envToIntegrationMap,
                                       @Qualifier("uiConfig") final UIConfiguration uiConfiguration,
                                       final IntegrationConfigMapper integrationConfigMapper,
                                       final LocatorSnapshotMapper locatorSnapshotMapper,
                                       final JacksonService jacksonService) {
        this.globalTestConfiguration = globalTestConfiguration;
        this.envToIntegrationMap = envToIntegrationMap;
        this.uiConfiguration = uiConfiguration;
        this.integrationConfigMapper = integrationConfigMapper;
        this.locatorSnapshotMapper = locatorSnapshotMapper;
        this.jacksonService = jacksonService;
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
                .tags(byTag.getTag().stream().filter(TagValue::isEnabled).map(TagValue::getName).toList())
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
                .ui(ui(uiConfiguration.get(folder)))
                .build();
    }

    private com.testlum.reporting.sdk.model.config.UiConfig ui(final UiConfig ui) {
        if (ui == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.UiConfig.builder()
                .web(web(ui.getWeb()))
                .mobilebrowser(mobilebrowser(ui.getMobilebrowser()))
                .nativeConfig(nativeConfig(ui.getNative()))
                .browserStackLogin(browserStackLogin(ui.getBrowserStackLogin()))
                .build();
    }

    private WebConfig web(final Web web) {
        if (web == null) {
            return null;
        }
        return WebConfig.builder()
                .enabled(web.isEnabled())
                .baseUrl(web.getBaseUrl())
                .details(jacksonService.toMapByFields(web))
                .build();
    }

    private MobileBrowserConfig mobilebrowser(final Mobilebrowser mobilebrowser) {
        if (mobilebrowser == null) {
            return null;
        }
        return MobileBrowserConfig.builder()
                .enabled(mobilebrowser.isEnabled())
                .baseUrl(mobilebrowser.getBaseUrl())
                .details(jacksonService.toMapByFields(mobilebrowser))
                .build();
    }

    private NativeConfig nativeConfig(final Native nativeConfig) {
        if (nativeConfig == null) {
            return null;
        }
        return NativeConfig.builder()
                .enabled(nativeConfig.isEnabled())
                .details(jacksonService.toMapByFields(nativeConfig))
                .build();
    }

    private com.testlum.reporting.sdk.model.config.BrowserStackLogin browserStackLogin(final BrowserStackLogin login) {
        if (login == null) {
            return null;
        }
        return com.testlum.reporting.sdk.model.config.BrowserStackLogin.builder()
                .username(login.getUsername())
                .accessKey(login.getAccessKey())
                .build();
    }
}
