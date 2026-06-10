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
        String aliases = joinValues(web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari(),
                AbstractBrowser::getAlias);
        String browserTypes = joinValues(web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari(),
                browser -> browser.getClass().getSimpleName());
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_WEB_ROW);
        addWebHeaders(table);
        addWebDataRow(table, web, aliases, browserTypes);
    }

    private void addMobileBrowserConfiguration(final DynamicTableBuilder table, final Mobilebrowser mobilebrowser) {
        if (mobilebrowser == null) {
            return;
        }
        String aliases = joinValues(mobilebrowser.getDevices().getDevice(), AbstractDevice::getAlias);
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_MOBILE_BROWSER_ROW);
        addMobileHeaders(table);
        addMobileDataRow(table, mobilebrowser, aliases);
    }

    private void addNativeConfiguration(final DynamicTableBuilder table, final Native nativeConfiguration) {
        if (nativeConfiguration == null) {
            return;
        }
        String aliases = joinValues(nativeConfiguration.getDevices().getDevice(), AbstractDevice::getAlias);
        String platforms = joinValues(nativeConfiguration.getDevices().getDevice(),
                abstractDevice -> abstractDevice.getPlatformName().value());
        addSectionTitle(table, LogMessage.UI_CONFIG_TABLE_NATIVE_ROW);
        addNativeHeaders(table);
        addNativeDataRow(table, nativeConfiguration, platforms, aliases);
    }

    private void addWebHeaders(final DynamicTableBuilder table) {
        table.row(
                LogMessage.UI_CONFIG_TABLE_BASE_URL_HEADER,
                LogMessage.UI_CONFIG_TABLE_BROWSER_ALIAS_HEADER,
                LogMessage.UI_CONFIG_TABLE_BROWSER_TYPE_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER
        );
    }

    private void addWebDataRow(final DynamicTableBuilder table,
                               final Web web,
                               final String aliases,
                               final String browserTypes) {
        table.row(
                computeRowColor(web.isEnabled()),
                web.getBaseUrl(),
                aliases,
                browserTypes,
                web.isEnabled()
        );
    }

    private void addMobileHeaders(final DynamicTableBuilder table) {
        table.row(
                LogMessage.UI_CONFIG_TABLE_BASE_URL_HEADER,
                LogMessage.UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER,
                LogMessage.UI_CONFIG_TABLE_CONNECTION_TYPE_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER
        );
    }

    private void addMobileDataRow(final DynamicTableBuilder table,
                                  final Mobilebrowser mobilebrowser,
                                  final String aliases) {
        table.row(
                computeRowColor(mobilebrowser.isEnabled()),
                mobilebrowser.getBaseUrl(),
                aliases,
                computeConnectionType(mobilebrowser.getConnection()),
                mobilebrowser.isEnabled()
        );
    }

    private void addNativeHeaders(final DynamicTableBuilder table) {
        table.row(
                LogMessage.UI_CONFIG_TABLE_NATIVE_PLATFORM_HEADER,
                LogMessage.UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER,
                LogMessage.UI_CONFIG_TABLE_CONNECTION_TYPE_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER
        );
    }

    private void addNativeDataRow(final DynamicTableBuilder table,
                                  final Native nativeConfiguration,
                                  final String platforms,
                                  final String aliases) {
        table.row(
                computeRowColor(nativeConfiguration.isEnabled()),
                platforms,
                aliases,
                computeConnectionType(nativeConfiguration.getConnection()),
                nativeConfiguration.isEnabled()
        );
    }

    private void addSectionTitle(final DynamicTableBuilder table, final String title) {
        table.row(Color.NONE, null, null, null, title);
    }

    private <T> String joinValues(final List<T> items, final Function<? super T, String> getter) {
        return items.stream()
                .map(getter)
                .collect(Collectors.joining(LogMessage.EMPTY_DELIMITER));
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
