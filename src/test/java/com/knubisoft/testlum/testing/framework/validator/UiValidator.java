package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.AppiumCapabilities;
import com.knubisoft.testlum.testing.model.global_config.AppiumNativeCapabilities;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackCapabilities;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackLogin;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings.UI_CONFIG_FILENAME;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BASE_URLS_ARE_SAME;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BROWSERSTACK_LOGIN_NOT_CONFIGURED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CONNECTION_TYPE_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DEVICE_PLATFORMS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ENVIRONMENT_MISSING_DEVICES_OR_BROWSERS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_APPIUM_SERVER_URLS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UI_CONFIG_ALIASES_NOT_DIFFER;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UI_CONFIG_ALIASES_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UI_CONFIG_NOT_PRESENT_IN_ALL_ENVS;
import static com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType.MOBILE_BROWSER;
import static com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType.NATIVE;
import static com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType.WEB;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class UiValidator {

    private static final Map<String, Map<UiConfigPredicate, Function<UiConfig, ?>>> UI_CONFIG_METHOD_MAP;
    private static final Map<String, Map<UiConfigPredicate, UiConfigToBaseurl>> BASE_URL_METHOD_MAP;
    private static final Map<String, Map<UiConfigPredicate, UiConfigToConnectionType>> CONNECTION_METHOD_MAP;
    private static final String DEVICE = "device";
    private static final String BROWSER = "browser";

    static {
        final Map<String, Map<UiConfigPredicate, Function<UiConfig, ?>>> configMethodMap = new HashMap<>();
        configMethodMap.put(WEB.name(), new HashMap<UiConfigPredicate, Function<UiConfig, ?>>() {{
            put(c -> nonNull(c.getWeb()) && c.getWeb().isEnabled(), UiConfig::getWeb);
        }});
        configMethodMap.put(NATIVE.name(), new HashMap<UiConfigPredicate, Function<UiConfig, ?>>() {{
            put(c -> nonNull(c.getNative()) && c.getNative().isEnabled(), UiConfig::getNative);
        }});
        configMethodMap.put(MOBILE_BROWSER.name(), new HashMap<UiConfigPredicate, Function<UiConfig, ?>>() {{
            put(c -> nonNull(c.getMobilebrowser()) && c.getMobilebrowser().isEnabled(), UiConfig::getMobilebrowser);
        }});
        UI_CONFIG_METHOD_MAP = Collections.unmodifiableMap(configMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToBaseurl>> baseUrlMethodMap = new HashMap<>();
        baseUrlMethodMap.put(WEB.name(), new HashMap<UiConfigPredicate, UiConfigToBaseurl>() {{
            put(c -> nonNull(c.getWeb()) && c.getWeb().isEnabled(), c -> c.getWeb().getBaseUrl());
        }});
        baseUrlMethodMap.put(MOBILE_BROWSER.name(), new HashMap<UiConfigPredicate, UiConfigToBaseurl>() {{
            put(c -> nonNull(c.getMobilebrowser()) && c.getMobilebrowser().isEnabled(),
                    c -> c.getMobilebrowser().getBaseUrl());
        }});
        BASE_URL_METHOD_MAP = Collections.unmodifiableMap(baseUrlMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToConnectionType>> cTMethodMap = new HashMap<>();
        cTMethodMap.put(NATIVE.name(), new HashMap<UiConfigPredicate, UiConfigToConnectionType>() {{
            put(c -> nonNull(c.getNative()) && c.getNative().isEnabled(), c -> c.getNative().getConnection());
        }});
        cTMethodMap.put(MOBILE_BROWSER.name(), new HashMap<UiConfigPredicate, UiConfigToConnectionType>() {{
            put(c -> nonNull(c.getMobilebrowser()) && c.getMobilebrowser().isEnabled(),
                    c -> c.getMobilebrowser().getConnection());
        }});
        CONNECTION_METHOD_MAP = Collections.unmodifiableMap(cTMethodMap);
    }

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());
        List<UiConfig> uiConfigList = new ArrayList<>(uiConfigMap.values());
        if (!uiConfigList.isEmpty()) {
            validateConfigsPresence(envList.size(), uiConfigList);
            validateBaseUrl(envList.size(), uiConfigList);
            validateConnection(uiConfigList);
            validateDevicesAndBrowsers(envList, uiConfigList);
        }
    }

    private void validateConfigsPresence(final int envNum, final List<UiConfig> uiConfigList) {
        UI_CONFIG_METHOD_MAP.forEach((configName, configMap) -> configMap.forEach((nonNullPredicate, configMethod) -> {
            List<?> uiConfig = uiConfigList.stream()
                    .filter(nonNullPredicate)
                    .map(configMethod)
                    .collect(Collectors.toList());
            if (!uiConfig.isEmpty() && uiConfig.size() != envNum) {
                throw new DefaultFrameworkException(UI_CONFIG_NOT_PRESENT_IN_ALL_ENVS, configName);
            }
        }));
    }

    private void validateBaseUrl(final int envNum, final List<UiConfig> uiConfig) {
        BASE_URL_METHOD_MAP.forEach((configName, urlMap) -> urlMap.forEach((nonNullPredicate, baseUrlMethod) -> {
            List<String> baseUrlList = uiConfig.stream()
                    .filter(nonNullPredicate)
                    .map(baseUrlMethod)
                    .collect(Collectors.toList());
            if (!baseUrlList.isEmpty() && baseUrlList.stream().distinct().count() != envNum) {
                throw new DefaultFrameworkException(BASE_URLS_ARE_SAME, configName);
            }
        }));
    }

    private void validateConnection(final List<UiConfig> uiConfigList) {
        CONNECTION_METHOD_MAP.forEach((configName, cTMap) -> cTMap.forEach((nonNullPredicate, connectionTypeMethod) -> {
            List<ConnectionType> cTList = uiConfigList.stream()
                    .filter(nonNullPredicate)
                    .map(connectionTypeMethod)
                    .collect(Collectors.toList());
            checkAppiumAndBrowserStackConnection(configName, uiConfigList, cTList);
        }));
    }

    private void checkAppiumAndBrowserStackConnection(final String configName,
                                                      final List<UiConfig> uiConfigList,
                                                      final List<ConnectionType> cTList) {
        if (cTList.stream().allMatch(connectionType -> nonNull(connectionType.getAppiumServer()))) {
            checkAppiumServerUrl(configName, cTList);
        } else if (cTList.stream().allMatch(connectionType -> nonNull(connectionType.getBrowserStack()))) {
            checkBrowserStackLogin(uiConfigList);
        } else {
            throw new DefaultFrameworkException(CONNECTION_TYPE_NOT_MATCH, configName);
        }
    }

    private void checkAppiumServerUrl(final String configName,
                                      final List<ConnectionType> connectionTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        for (ConnectionType connectionType : connectionTypeList) {
            String serverUrl = connectionType.getAppiumServer().getServerUrl();
            if (!serverUrlList.add(serverUrl)) {
                throw new DefaultFrameworkException(SAME_APPIUM_SERVER_URLS, configName);
            }
        }
    }

    private void checkBrowserStackLogin(final List<UiConfig> uiConfigList) {
        List<BrowserStackLogin> browserStackLoginList = uiConfigList.stream()
                .map(UiConfig::getBrowserStackLogin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (browserStackLoginList.size() != uiConfigList.size()) {
            throw new DefaultFrameworkException(BROWSERSTACK_LOGIN_NOT_CONFIGURED);
        }
    }

    private void validateDevicesAndBrowsers(final List<String> envList, final List<UiConfig> uiConfigs) {
        UI_CONFIG_METHOD_MAP.forEach((configName, configMap) -> configMap.forEach((nullCheck, configMethod) -> {
            List<? extends List<?>> deviceOrBrowserList = getDevicesOrBrowsers(uiConfigs, nullCheck, configMethod);
            if (!deviceOrBrowserList.isEmpty() && deviceOrBrowserList.size() != envList.size()) {
                throw new DefaultFrameworkException(ENVIRONMENT_MISSING_DEVICES_OR_BROWSERS,
                        getName(deviceOrBrowserList.get(0).get(0)), configName);
            } else if (!deviceOrBrowserList.isEmpty()) {
                validateDevicesAndBrowsers(configName, envList, uiConfigs, deviceOrBrowserList);
            }
        }));
    }

    private void validateDevicesAndBrowsers(final String configName,
                                            final List<String> envList,
                                            final List<UiConfig> uiConfigs,
                                            final List<? extends List<?>> deviceOrBrowserList) {
        List<?> defaultDevicesOrBrowsers = getDefaultDevicesOrBrowsers(deviceOrBrowserList);
        checkAliasesDifferAndMatch(configName, envList, defaultDevicesOrBrowsers, deviceOrBrowserList);
        if (configName.equals(NATIVE.name()) || configName.equals(MOBILE_BROWSER.name())) {
            List<? extends AbstractDevice> defaultDevices = (List<? extends AbstractDevice>) defaultDevicesOrBrowsers;
            List<? extends List<? extends AbstractDevice>> deviceList =
                    (List<? extends List<? extends AbstractDevice>>) deviceOrBrowserList;
            checkPlatformNameMatch(configName, defaultDevices, deviceList);
            validateDeviceCapabilities(configName, envList, uiConfigs, defaultDevices, deviceList);
        }
    }

    private List<? extends List<?>> getDevicesOrBrowsers(final List<UiConfig> uiConfigs,
                                                         final UiConfigPredicate nonNullPredicate,
                                                         final Function<UiConfig, ?> configMethod) {
        return uiConfigs.stream()
                .filter(nonNullPredicate)
                .map(configMethod)
                .map(config -> getDeviceOrBrowserList(config).stream()
                        .filter(this::isDeviceOrBrowserEnabled)
                        .collect(Collectors.toList()))
                .filter(deviceOrBrowserList -> !deviceOrBrowserList.isEmpty())
                .collect(Collectors.toList());
    }

    private <T> List<?> getDeviceOrBrowserList(final T config) {
        return config instanceof Web ? ((Web) config).getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari()
                : config instanceof Native ? ((Native) config).getDevices().getDevice()
                : ((Mobilebrowser) config).getDevices().getDevice();
    }

    private <T> boolean isDeviceOrBrowserEnabled(final T deviceOrBrowser) {
        return deviceOrBrowser instanceof AbstractDevice ? ((AbstractDevice) deviceOrBrowser).isEnabled()
                : ((AbstractBrowser) deviceOrBrowser).isEnabled();
    }

    private <T> List<?> getDefaultDevicesOrBrowsers(final List<? extends List<? extends T>> deviceOrBrowserList) {
        return deviceOrBrowserList.stream()
                .min(Comparator.comparingInt((List<?> devicesOrdBrowsers) -> devicesOrdBrowsers.size())).get();
    }

    private <T> void checkAliasesDifferAndMatch(final String configName,
                                                final List<String> envsList,
                                                final List<?> defaultDevicesOrBrowsersList,
                                                final List<? extends List<? extends T>> deviceOrBrowserList) {
        List<String> defaultAliasesList = getDefaultAliasesList(defaultDevicesOrBrowsersList);
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (T deviceOrBrowser : deviceOrBrowserList.get(envNum)) {
                if (!aliasSet.add(getAlias(deviceOrBrowser))) {
                    throw new DefaultFrameworkException(UI_CONFIG_ALIASES_NOT_DIFFER, getName(deviceOrBrowser),
                            configName, getAlias(deviceOrBrowser), getConfigPath(envsList, envNum));
                } else if (!defaultAliasesList.contains(getAlias(deviceOrBrowser))) {
                    throw new DefaultFrameworkException(UI_CONFIG_ALIASES_NOT_MATCH, getName(deviceOrBrowser),
                            configName, getAlias(deviceOrBrowser));
                }
            }
        }
    }

    private <T> String getAlias(final T deviceOrBrowser) {
        return deviceOrBrowser instanceof AbstractDevice
                ? ((AbstractDevice) deviceOrBrowser).getAlias() : ((AbstractBrowser) deviceOrBrowser).getAlias();
    }

    private <T> String getName(final T deviceOrBrowser) {
        return deviceOrBrowser instanceof AbstractDevice ? DEVICE : BROWSER;
    }

    private String getConfigPath(final List<String> envsList, final int envNum) {
        return FileSearcher.searchFileFromEnvFolder(envsList.get(envNum), UI_CONFIG_FILENAME).get().getPath()
                .replace(TestResourceSettings.getInstance().getTestResourcesFolder().getPath(), StringUtils.EMPTY);
    }

    private static <T> List<String> getDefaultAliasesList(final List<T> defaultAliasesList) {
        return defaultAliasesList.get(0) instanceof AbstractDevice
                ? ((List<AbstractDevice>) defaultAliasesList).stream()
                .map(AbstractDevice::getAlias).collect(Collectors.toList())
                : ((List<AbstractBrowser>) defaultAliasesList).stream()
                .map(AbstractBrowser::getAlias).collect(Collectors.toList());
    }

    private void validateDeviceCapabilities(final String configName,
                                            final List<String> envList,
                                            final List<UiConfig> uiConfigs,
                                            final List<? extends AbstractDevice> defaultDeviceList,
                                            final List<? extends List<? extends AbstractDevice>> deviceList) {
        if (configName.equals(MOBILE_BROWSER.name())) {
            checkMobilebrowserCapabilities(configName, envList, uiConfigs, getAllMobilbrowserDevices(deviceList),
                    getDefaultMobilebrowserDeviceMap(defaultDeviceList));
        }
        if (configName.equals(NATIVE.name())) {
            checkNativeCapabilities(configName, envList, uiConfigs, getAllNativeDevices(deviceList),
                    getDefaultNativeDeviceMap(defaultDeviceList));
        }
    }

    private void checkPlatformNameMatch(final String configName,
                                        final List<? extends AbstractDevice> defaultDevicesList,
                                        final List<? extends List<? extends AbstractDevice>> devicesList) {
        for (List<? extends AbstractDevice> devices : devicesList) {
            for (AbstractDevice device : devices) {
                if (defaultDevicesList.stream()
                        .anyMatch(d -> !d.getPlatformName().value().equals(device.getPlatformName().value()))) {
                    throw new DefaultFrameworkException(DEVICE_PLATFORMS_NOT_MATCH, configName, device.getAlias());
                }
            }
        }
    }

    private List<List<MobilebrowserDevice>> getAllMobilbrowserDevices(
            final List<? extends List<? extends AbstractDevice>> deviceList) {
        return deviceList.stream()
                .map(a -> a.stream()
                        .map(device -> (MobilebrowserDevice) device)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, MobilebrowserDevice> getDefaultMobilebrowserDeviceMap(
            final List<? extends AbstractDevice> defaultDevices) {
        return defaultDevices.stream()
                .map(device -> (MobilebrowserDevice) device)
                .collect(Collectors.toMap(AbstractDevice::getAlias, mobilebrowserDevice -> mobilebrowserDevice));
    }

    private void checkMobilebrowserCapabilities(final String configName,
                                                final List<String> envList,
                                                final List<UiConfig> uiConfigs,
                                                final List<List<MobilebrowserDevice>> mobilebrowserList,
                                                final Map<String, MobilebrowserDevice> mBDeviceDefaultMap) {
        for (int envNum = 0; envNum < uiConfigs.size(); envNum++) {
            for (MobilebrowserDevice mobileDevice : mobilebrowserList.get(envNum)) {
                ConnectionType cT = uiConfigs.get(envNum).getMobilebrowser().getConnection();
                MobilebrowserDevice defaultDevice = mBDeviceDefaultMap.get(mobileDevice.getAlias());
                checkMobilebrowserAppiumCapabilities(envNum, configName, cT,
                        envList, mobileDevice, defaultDevice.getAppiumCapabilities());
                checkMobilebrowserBrowserStackCapapabilities(envNum, configName, cT,
                        envList, mobileDevice, defaultDevice.getBrowserStackCapabilities());
            }
        }
    }

    private void checkMobilebrowserAppiumCapabilities(final int envNum,
                                                      final String configName,
                                                      final ConnectionType cT,
                                                      final List<String> envList,
                                                      final MobilebrowserDevice device,
                                                      final AppiumCapabilities defaultAppiumCapabilities) {
        if (nonNull(cT.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(cT.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkMobilebrowserBrowserStackCapapabilities(final int envNum,
                                                              final String configName,
                                                              final ConnectionType cT,
                                                              final List<String> envList,
                                                              final MobilebrowserDevice device,
                                                              final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(cT.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(cT.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private List<List<NativeDevice>> getAllNativeDevices(
            final List<? extends List<? extends AbstractDevice>> deviceList) {
        return deviceList.stream()
                .map(a -> a.stream()
                        .map(device -> (NativeDevice) device)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, NativeDevice> getDefaultNativeDeviceMap(
            final List<? extends AbstractDevice> defaultDevices) {
        return defaultDevices.stream()
                .map(device -> (NativeDevice) device)
                .collect(Collectors.toMap(AbstractDevice::getAlias, nativeDevice -> nativeDevice));
    }

    private void checkNativeCapabilities(final String configName,
                                         final List<String> envList,
                                         final List<UiConfig> uiConfigs,
                                         final List<List<NativeDevice>> nativeList,
                                         final Map<String, NativeDevice> nativeDeviceDefaultMap) {
        for (int envNum = 0; envNum < uiConfigs.size(); envNum++) {
            for (NativeDevice nativeDevice : nativeList.get(envNum)) {
                ConnectionType cT = uiConfigs.get(envNum).getNative().getConnection();
                NativeDevice defaultDevice = nativeDeviceDefaultMap.get(nativeDevice.getAlias());
                checkNativeAppiumCapabilities(envNum, configName, cT,
                        envList, nativeDevice, defaultDevice.getAppiumCapabilities());
                checkNativeBrowserStackCapabilities(envNum, configName, cT,
                        envList, nativeDevice, defaultDevice.getBrowserStackCapabilities());
            }
        }
    }

    private void checkNativeAppiumCapabilities(final int envNum,
                                               final String configName,
                                               final ConnectionType cT,
                                               final List<String> envList,
                                               final NativeDevice device,
                                               final AppiumNativeCapabilities defaultAppiumCapabilities) {
        if (nonNull(cT.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(cT.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkNativeBrowserStackCapabilities(final int envNum,
                                                     final String configName,
                                                     final ConnectionType cT,
                                                     final List<String> envList,
                                                     final NativeDevice device,
                                                     final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(cT.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(cT.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private interface UiConfigPredicate extends Predicate<UiConfig> { }
    private interface UiConfigToBaseurl extends Function<UiConfig, String> { }
    private interface UiConfigToConnectionType extends Function<UiConfig, ConnectionType> { }
}
