package com.knubisoft.testlum.testing.logger;

import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.log.table.TableBuilder;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
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

    public void logIntegrationConfiguration(final Map<String, Integrations> integrationsMap) {
        DynamicTableBuilder table = TableBuilder.grid(LogMessage.INTEGRATION_CONFIG_TABLE_TITLE);
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
        DynamicTableBuilder table = TableBuilder.grid(LogMessage.UI_CONFIG_TABLE_TITLE);
        uiConfigMap.forEach(
                (environment, uiConfig) ->
                        this.uiConfigurationLogger.appendEnvironmentSections(table, environment, uiConfig)
        );
        if (!table.getRows().isEmpty()) {
            log.info(table.build());
        }
    }

}
