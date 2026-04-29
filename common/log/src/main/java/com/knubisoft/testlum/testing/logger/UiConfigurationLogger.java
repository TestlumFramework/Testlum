package com.knubisoft.testlum.testing.logger;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UiConfigurationLogger {

    public void appendEnvironmentSections(final DynamicTableBuilder table,
                                          final String environment,
                                          final UiConfig uiConfig) {
        if (!hasAnyUiSection(uiConfig)) {
            return;
        }
        table.columns(null, null, null, String.format(LogMessage.UI_CONFIG_TABLE_ENV_ROW, environment));
        addWebConfiguration(table, uiConfig.getWeb());
        addMobileBrowserConfiguration(table, uiConfig.getMobilebrowser());
        addNativeConfiguration(table, uiConfig.getNative());
    }

    private boolean hasAnyUiSection(final UiConfig uiConfig) {
        return uiConfig.getWeb() != null
               || uiConfig.getMobilebrowser() != null
               || uiConfig.getNative() != null;
    }

    private void addWebConfiguration(final DynamicTableBuilder table, final Web web) {
        if (web == null) {
            return;
        }
        String aliases = joinAliases(web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari(),
                AbstractBrowser::getAlias);
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_WEB_ROW);
        table.row(Color.NONE, null, LogMessage.UI_CONFIG_TABLE_BASE_URL_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER, LogMessage.UI_CONFIG_TABLE_BROWSER_ALIAS_HEADER);
        table.row(computeRowColor(web.isEnabled()), null, web.getBaseUrl(), web.isEnabled(), aliases);
    }

    private void addMobileBrowserConfiguration(final DynamicTableBuilder table, final Mobilebrowser mobilebrowser) {
        if (mobilebrowser == null) {
            return;
        }
        String aliases = joinAliases(mobilebrowser.getDevices().getDevice(), AbstractDevice::getAlias);
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_MOBILE_BROWSER_ROW);
        table.row(LogMessage.UI_CONFIG_TABLE_BASE_URL_HEADER, LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER,
                LogMessage.UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER, LogMessage.UI_CONFIG_TABLE_CONNECTION_TYPE_HEADER);
        table.row(computeRowColor(mobilebrowser.isEnabled()), mobilebrowser.getBaseUrl(), mobilebrowser.isEnabled(),
                aliases, computeConnectionType(mobilebrowser.getConnection()));
    }

    private void addNativeConfiguration(final DynamicTableBuilder table, final Native nativeConfiguration) {
        if (nativeConfiguration == null) {
            return;
        }
        String aliases = joinAliases(nativeConfiguration.getDevices().getDevice(), AbstractDevice::getAlias);
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_NATIVE_ROW);
        table.row(Color.NONE, null, LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER,
                LogMessage.UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER, LogMessage.UI_CONFIG_TABLE_CONNECTION_TYPE_HEADER);
        table.row(computeRowColor(nativeConfiguration.isEnabled()), null, nativeConfiguration.isEnabled(), aliases,
                computeConnectionType(nativeConfiguration.getConnection()));
    }

    private void addSectionTitle(final DynamicTableBuilder table, final String title) {
        table.row(Color.NONE, null, null, null, title);
    }

    private <T> String joinAliases(final List<T> items, final Function<? super T, String> aliasGetter) {
        return items.stream()
                .map(aliasGetter)
                .collect(Collectors.joining(LogMessage.ALIAS_DELIMITER));
    }

    private String computeConnectionType(final ConnectionType connectionType) {
        return connectionType.getAppiumServer() != null
                ? LogMessage.CONNECTION_APPIUM_SERVER
                : LogMessage.CONNECTION_BROWSER_STACK;
    }

    private Color computeRowColor(final boolean isEnabled) {
        if (isEnabled) {
            return Color.GREEN;
        }
        return Color.RED;
    }

}
