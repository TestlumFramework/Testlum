package com.testlum.testing.logger;

import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.log.table.TableBuilder;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.RunScenariosByTag;
import com.testlum.testing.model.global_config.UiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConfigurationLogger {

    private final UiConfigurationLogger uiConfigurationLogger;
    private final IntegrationConfigurationLogger integrationConfigurationLogger;
    private final TagConfigurationLogger tagConfigurationLogger;

    public void logIntegrationConfiguration(final Map<String, Integrations> integrationsMap) {
        DynamicTableBuilder table = TableBuilder.grid(LogMessage.INTEGRATION_CONFIG_TABLE_TITLE)
                .columnCount(IntegrationConfigurationLogger.COLUMN_COUNT);
        integrationsMap.forEach(
                (environment, integrations) ->
                        this.integrationConfigurationLogger
                                .appendEnvironmentSections(table, environment, integrations)
        );
        if (!table.getRows().isEmpty()) {
            log.info(table.build());
        }
    }

    public void logUiConfiguration(final Map<String, UiConfig> uiConfigMap) {
        DynamicTableBuilder table = TableBuilder.grid(LogMessage.UI_CONFIG_TABLE_TITLE)
                .columnCount(UiConfigurationLogger.COLUMN_COUNT);
        uiConfigMap.forEach(
                (environment, uiConfig) ->
                        this.uiConfigurationLogger.appendEnvironmentSections(table, environment, uiConfig)
        );
        if (!table.getRows().isEmpty()) {
            log.info(table.build());
        }
    }

    public void logTagConfiguration(final RunScenariosByTag runScenariosByTag,
                                    final Map<String, Long> scenarioCountByTag) {
        if (runScenariosByTag == null) {
            return;
        }
        if (!runScenariosByTag.isEnabled()) {
            log.info(LogMessage.TAG_FILTERING_DISABLED);
            return;
        }
        DynamicTableBuilder table = TableBuilder.grid(LogMessage.TAG_CONFIG_TABLE_TITLE)
                .columnCount(TagConfigurationLogger.COLUMN_COUNT);
        this.tagConfigurationLogger.appendSections(table, runScenariosByTag, scenarioCountByTag);
        if (!table.getRows().isEmpty()) {
            log.info(table.build());
        }
    }

}
