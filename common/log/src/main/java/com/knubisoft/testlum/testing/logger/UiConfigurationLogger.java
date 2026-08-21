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

@Component
@RequiredArgsConstructor
public class UiConfigurationLogger {

    public static final int COLUMN_COUNT = 3;

    public void appendEnvironmentSections(final DynamicTableBuilder table,
                                          final String environment,
                                          final UiConfig uiConfig) {
        if (!hasAnyUiSection(uiConfig)) {
            return;
        }
        table.span(String.format(LogMessage.UI_CONFIG_TABLE_ENV_ROW, environment));
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
        addSectionHeader(table, LogMessage.UI_CONFIG_TABLE_WEB_ROW, web.isEnabled());
        table.span(String.format(LogMessage.UI_CONFIG_TABLE_BASE_URL_ROW, web.getBaseUrl()));
        addBrowserSection(table, web.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari());
    }

    private void addBrowserSection(final DynamicTableBuilder table,
                                   final List<? extends AbstractBrowser> browsers) {
        table.row(LogMessage.UI_CONFIG_TABLE_BROWSER_HEADER,
                LogMessage.UI_CONFIG_TABLE_ALIAS_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER);
        for (AbstractBrowser browser : browsers) {
            table.row(computeRowColor(browser.isEnabled()),
                    browser.getClass().getSimpleName(),
                    browser.getAlias(),
                    browser.isEnabled());
        }
    }

    private void addMobileBrowserConfiguration(final DynamicTableBuilder table, final Mobilebrowser mobilebrowser) {
        if (mobilebrowser == null) {
            return;
        }
        addSectionHeader(table, LogMessage.UI_CONFIG_TABLE_MOBILE_BROWSER_ROW, mobilebrowser.isEnabled());
        table.span(String.format(LogMessage.UI_CONFIG_TABLE_BASE_URL_ROW, mobilebrowser.getBaseUrl()));
        addDeviceSection(table, mobilebrowser.getConnection(), mobilebrowser.getDevices().getDevice());
    }

    private void addNativeConfiguration(final DynamicTableBuilder table, final Native nativeConfiguration) {
        if (nativeConfiguration == null) {
            return;
        }
        addSectionHeader(table, LogMessage.UI_CONFIG_TABLE_NATIVE_ROW, nativeConfiguration.isEnabled());
        addDeviceSection(table, nativeConfiguration.getConnection(), nativeConfiguration.getDevices().getDevice());
    }

    private void addDeviceSection(final DynamicTableBuilder table,
                                  final ConnectionType connection,
                                  final List<? extends AbstractDevice> devices) {
        table.span(String.format(LogMessage.UI_CONFIG_TABLE_CONNECTION_TYPE_ROW,
                computeConnectionType(connection)));
        table.row(LogMessage.UI_CONFIG_TABLE_PLATFORM_HEADER,
                LogMessage.UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER,
                LogMessage.UI_CONFIG_TABLE_ENABLED_HEADER);
        for (AbstractDevice device : devices) {
            table.row(computeRowColor(device.isEnabled()),
                    device.getPlatformName().value(),
                    device.getAlias(),
                    device.isEnabled());
        }
    }

    private void addSectionHeader(final DynamicTableBuilder table,
                                  final String titleFormat,
                                  final boolean isEnabled) {
        String enabledDetail = isEnabled
                ? LogMessage.UI_CONFIG_TABLE_ENABLED_DETAIL
                : LogMessage.UI_CONFIG_TABLE_DISABLED_DETAIL;
        table.span(computeRowColor(isEnabled), String.format(titleFormat, enabledDetail));
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
