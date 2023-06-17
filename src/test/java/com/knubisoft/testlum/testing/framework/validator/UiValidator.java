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
import com.knubisoft.testlum.testing.model.global_config.BrowserStackNativeCapabilities;
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
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UI_ALIASES_NOT_DIFFER;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UI_ALIASES_NOT_MATCH;
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
        configMethodMap.put(WEB.name(), Collections.singletonMap(c -> nonNull(c.getWeb())
                && c.getWeb().isEnabled(), UiConfig::getWeb));
        configMethodMap.put(NATIVE.name(), Collections.singletonMap(c -> nonNull(c.getNative())
                && c.getNative().isEnabled(), UiConfig::getNative));
        configMethodMap.put(MOBILE_BROWSER.name(), Collections.singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), UiConfig::getMobilebrowser));
        UI_CONFIG_METHOD_MAP = Collections.unmodifiableMap(configMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToBaseurl>> baseUrlMethodMap = new HashMap<>();
        baseUrlMethodMap.put(WEB.name(), Collections.singletonMap(c -> nonNull(c.getWeb())
                && c.getWeb().isEnabled(), c -> c.getWeb().getBaseUrl()));
        baseUrlMethodMap.put(MOBILE_BROWSER.name(), Collections.singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), c -> c.getMobilebrowser().getBaseUrl()));
        BASE_URL_METHOD_MAP = Collections.unmodifiableMap(baseUrlMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToConnectionType>> cTMethodMap = new HashMap<>();
        cTMethodMap.put(NATIVE.name(), Collections.singletonMap(c -> nonNull(c.getNative())
                && c.getNative().isEnabled(), c -> c.getNative().getConnection()));
        cTMethodMap.put(MOBILE_BROWSER.name(), Collections.singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), c -> c.getMobilebrowser().getConnection()));
        CONNECTION_METHOD_MAP = Collections.unmodifiableMap(cTMethodMap);
    }

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());
        List<UiConfig> uiConfigList = new ArrayList<>(uiConfigMap.values());
        if (!uiConfigList.isEmpty()) {
            validateNativeOrMobileOrWebPresence(envList.size(), uiConfigList);
            validateBaseUrl(envList.size(), uiConfigList);
            validateConnection(uiConfigList);
            validateDevicesAndBrowsers(envList, uiConfigList);
        }
    }

    private void validateNativeOrMobileOrWebPresence(final int envNum, final List<UiConfig> uiConfigList) {
        UI_CONFIG_METHOD_MAP.forEach((nativeOrMobileOrWebName, uiConfigsMap) ->
                uiConfigsMap.forEach((nonNullPredicate, uiConfigToNativeOrMobileOrWebMethod) -> {
                    List<?> nativeOrMobileOrWebList = uiConfigList.stream()
                            .filter(nonNullPredicate)
                            .map(uiConfigToNativeOrMobileOrWebMethod)
                            .collect(Collectors.toList());
                    if (!nativeOrMobileOrWebList.isEmpty() && nativeOrMobileOrWebList.size() != envNum) {
                        throw new DefaultFrameworkException(UI_CONFIG_NOT_PRESENT_IN_ALL_ENVS, nativeOrMobileOrWebName);
                    }
                }));
    }

    private void validateBaseUrl(final int envNum, final List<UiConfig> uiConfigList) {
        BASE_URL_METHOD_MAP.forEach((mobileOrWebConfigName, baseUrlMap) ->
                baseUrlMap.forEach((nonNullPredicate, baseUrlMethod) -> {
                    List<String> baseUrlList = uiConfigList.stream()
                            .filter(nonNullPredicate)
                            .map(baseUrlMethod)
                            .collect(Collectors.toList());
                    if (!baseUrlList.isEmpty() && baseUrlList.stream().distinct().count() != envNum) {
                        throw new DefaultFrameworkException(BASE_URLS_ARE_SAME, mobileOrWebConfigName);
                    }
                }));
    }

    private void validateConnection(final List<UiConfig> uiConfigList) {
        CONNECTION_METHOD_MAP.forEach((mobileOrNativeConfigName, connectionTypeMap) ->
                connectionTypeMap.forEach((nonNullPredicate, connectionTypeMethod) -> {
                    List<ConnectionType> connectionTypeList = uiConfigList.stream()
                            .filter(nonNullPredicate)
                            .map(connectionTypeMethod)
                            .collect(Collectors.toList());
                    checkConnectionType(mobileOrNativeConfigName, uiConfigList, connectionTypeList);
                }));
    }

    private void checkConnectionType(final String mobileOrNativeConfigName,
                                     final List<UiConfig> uiConfigList,
                                     final List<ConnectionType> connectionTypeList) {
        if (connectionTypeList.stream().allMatch(connectionType -> nonNull(connectionType.getAppiumServer()))) {
            checkAppiumServerUrl(mobileOrNativeConfigName, connectionTypeList);
        } else if (connectionTypeList.stream().allMatch(connectionType -> nonNull(connectionType.getBrowserStack()))) {
            checkBrowserStackLogin(uiConfigList);
        } else {
            throw new DefaultFrameworkException(CONNECTION_TYPE_NOT_MATCH, mobileOrNativeConfigName);
        }
    }

    private void checkAppiumServerUrl(final String mobileOrNativeConfigName,
                                      final List<ConnectionType> connectionTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        for (ConnectionType connectionType : connectionTypeList) {
            String serverUrl = connectionType.getAppiumServer().getServerUrl();
            if (!serverUrlList.add(serverUrl)) {
                throw new DefaultFrameworkException(SAME_APPIUM_SERVER_URLS, mobileOrNativeConfigName);
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

    private void validateDevicesAndBrowsers(final List<String> envList, final List<UiConfig> uiConfigList) {
        for (Map.Entry<String, Map<UiConfigPredicate, Function<UiConfig, ?>>> entry : UI_CONFIG_METHOD_MAP.entrySet()) {
            String nativeOrMobileOrWebName = entry.getKey();
            entry.getValue().forEach((notNullPredicate, getConfigMethod) -> {
                List<? extends List<?>> deviceOrBrowserLists =
                        getDevicesOrBrowsers(uiConfigList, notNullPredicate, getConfigMethod);
                if (!deviceOrBrowserLists.isEmpty() && deviceOrBrowserLists.size() != envList.size()) {
                    throw new DefaultFrameworkException(ENVIRONMENT_MISSING_DEVICES_OR_BROWSERS,
                            deviceOrBrowserLists.get(0).get(0).getClass().getSimpleName(), nativeOrMobileOrWebName);
                } else if (!deviceOrBrowserLists.isEmpty()) {
                    devicesAndBrowsersValidation(nativeOrMobileOrWebName, envList, uiConfigList, deviceOrBrowserLists);
                }
            });
        }
    }

    private List<? extends List<?>> getDevicesOrBrowsers(final List<UiConfig> uiConfigList,
                                                         final UiConfigPredicate nonNullPredicate,
                                                         final Function<UiConfig, ?> getConfigMethod) {
        return uiConfigList.stream()
                .filter(nonNullPredicate)
                .map(getConfigMethod)
                .map(webOrNativeOrMobileConfig -> getDeviceOrBrowserList(webOrNativeOrMobileConfig).stream()
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

    @SuppressWarnings("unchecked")
    private void devicesAndBrowsersValidation(final String configName,
                                              final List<String> envList,
                                              final List<UiConfig> uiConfigList,
                                              final List<? extends List<?>> deviceOrBrowserLists) {
        List<?> defaultDeviceOrBrowserList = deviceOrBrowserLists.stream()
                .min(Comparator.comparingInt((List<?> devicesOrdBrowsers) -> devicesOrdBrowsers.size())).get();
        if (configName.equals(WEB.name())) {
            Map<String, String> defaultBrowserMap =
                    ((List<? extends AbstractBrowser>) defaultDeviceOrBrowserList).stream()
                            .collect(Collectors.toMap(AbstractBrowser::getAlias, b -> b.getClass().getSimpleName()));
            checkBrowserAliasesDifferAndMatch(configName, envList, defaultBrowserMap,
                    (List<? extends List<? extends AbstractBrowser>>) deviceOrBrowserLists);
        } else {
            validateDevices(configName, envList, uiConfigList, defaultDeviceOrBrowserList, deviceOrBrowserLists);
        }
    }

    private void checkBrowserAliasesDifferAndMatch(final String configName,
                                                   final List<String> envList,
                                                   final Map<String, String> defaultBrowserMap,
                                                   final List<? extends List<? extends AbstractBrowser>> browserLists) {
        for (int envNum = 0; envNum < envList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (AbstractBrowser browser : browserLists.get(envNum)) {
                if (!aliasSet.add(browser.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_DIFFER, BROWSER,
                            configName, browser.getAlias(), getConfigPath(envList, envNum));
                } else if (!defaultBrowserMap.containsKey(browser.getAlias())
                        || !defaultBrowserMap.get(browser.getAlias()).equals(browser.getClass().getSimpleName())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_MATCH,
                            browser.getClass().getSimpleName(), BROWSER, configName, browser.getAlias());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void validateDevices(final String configName,
                                 final List<String> envList,
                                 final List<UiConfig> uiConfigList,
                                 final List<?> defaultDeviceOrBrowserList,
                                 final List<? extends List<?>> deviceOrBrowserLists) {
        List<? extends AbstractDevice> defaultDeviceList = (List<? extends AbstractDevice>) defaultDeviceOrBrowserList;
        List<? extends List<? extends AbstractDevice>> deviceLists =
                (List<? extends List<? extends AbstractDevice>>) deviceOrBrowserLists;
        checkDeviceAliasesDifferAndMatch(configName, envList, defaultDeviceList, deviceLists);
        checkPlatformNameMatch(configName, defaultDeviceList, deviceLists);
        validateDeviceCapabilities(configName, envList, uiConfigList, defaultDeviceList, deviceLists);
    }

    @SuppressWarnings("unchecked")
    private void checkDeviceAliasesDifferAndMatch(final String configName,
                                                  final List<String> envList,
                                                  final List<? extends AbstractDevice> defaultDevicesList,
                                                  final List<? extends List<? extends AbstractDevice>> deviceLists) {
        List<String> defaultAliasList = ((List<AbstractDevice>) defaultDevicesList).stream()
                .map(AbstractDevice::getAlias).collect(Collectors.toList());
        for (int envNum = 0; envNum < envList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (AbstractDevice device : deviceLists.get(envNum)) {
                if (!aliasSet.add(device.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_DIFFER, DEVICE,
                            configName, device.getAlias(), getConfigPath(envList, envNum));
                } else if (!defaultAliasList.contains(device.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_MATCH, DEVICE, configName, device.getAlias());
                }
            }
        }
    }

    private void checkPlatformNameMatch(final String configName,
                                        final List<? extends AbstractDevice> defaultDeviceList,
                                        final List<? extends List<? extends AbstractDevice>> devicesLists) {
        for (List<? extends AbstractDevice> devices : devicesLists) {
            for (AbstractDevice device : devices) {
                AbstractDevice defaultDevice = defaultDeviceList.stream()
                        .filter(d -> device.getAlias().equals(d.getAlias()))
                        .findFirst().get();
                if (!defaultDevice.getPlatformName().value().equals(device.getPlatformName().value())) {
                    throw new DefaultFrameworkException(DEVICE_PLATFORMS_NOT_MATCH, configName, device.getAlias());
                }
            }
        }
    }

    private void validateDeviceCapabilities(final String configName,
                                            final List<String> envList,
                                            final List<UiConfig> uiConfigs,
                                            final List<? extends AbstractDevice> defaultDeviceList,
                                            final List<? extends List<? extends AbstractDevice>> deviceLists) {
        if (configName.equals(MOBILE_BROWSER.name())) {
            List<List<MobilebrowserDevice>> mobilbrowserDevices = getAllMobilbrowserDevices(deviceLists);
            Map<String, MobilebrowserDevice> defaultMobileDeviceMap = getMobilebrowserDeviceMap(defaultDeviceList);
            checkMobilebrowserCapabilities(configName, envList, uiConfigs, mobilbrowserDevices, defaultMobileDeviceMap);
        }
        if (configName.equals(NATIVE.name())) {
            List<List<NativeDevice>> nativeDevices = getAllNativeDevices(deviceLists);
            Map<String, NativeDevice> defaultNativeDeviceMap = getDefaultNativeDeviceMap(defaultDeviceList);
            checkNativeCapabilities(configName, envList, uiConfigs, nativeDevices, defaultNativeDeviceMap);
        }
    }

    private List<List<MobilebrowserDevice>> getAllMobilbrowserDevices(
            final List<? extends List<? extends AbstractDevice>> deviceLists) {
        return deviceLists.stream()
                .map(a -> a.stream()
                        .map(device -> (MobilebrowserDevice) device)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, MobilebrowserDevice> getMobilebrowserDeviceMap(
            final List<? extends AbstractDevice> defaultDeviceList) {
        return defaultDeviceList.stream()
                .map(device -> (MobilebrowserDevice) device)
                .collect(Collectors.toMap(AbstractDevice::getAlias, mobilebrowserDevice -> mobilebrowserDevice));
    }

    private void checkMobilebrowserCapabilities(final String configName,
                                                final List<String> envList,
                                                final List<UiConfig> uiConfigList,
                                                final List<List<MobilebrowserDevice>> mobilebrowserDeviceLists,
                                                final Map<String, MobilebrowserDevice> mobilebrowserDeviceDefaultMap) {
        for (int envNum = 0; envNum < uiConfigList.size(); envNum++) {
            for (MobilebrowserDevice mobileDevice : mobilebrowserDeviceLists.get(envNum)) {
                ConnectionType connectionType = uiConfigList.get(envNum).getMobilebrowser().getConnection();
                MobilebrowserDevice defaultDevice = mobilebrowserDeviceDefaultMap.get(mobileDevice.getAlias());
                checkMobilebrowserAppiumCapabilities(envNum, configName, connectionType,
                        envList, mobileDevice, defaultDevice.getAppiumCapabilities());
                checkMobilebrowserBrowserStackCapabilities(envNum, configName, connectionType,
                        envList, mobileDevice, defaultDevice.getBrowserStackCapabilities());
            }
        }
    }

    private void checkMobilebrowserAppiumCapabilities(final int envNum,
                                                      final String configName,
                                                      final ConnectionType connectionType,
                                                      final List<String> envList,
                                                      final MobilebrowserDevice device,
                                                      final AppiumCapabilities defaultAppiumCapabilities) {
        if (nonNull(connectionType.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(connectionType.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkMobilebrowserBrowserStackCapabilities(final int envNum,
                                                            final String configName,
                                                            final ConnectionType connectionType,
                                                            final List<String> envList,
                                                            final MobilebrowserDevice device,
                                                            final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(connectionType.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(connectionType.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private List<List<NativeDevice>> getAllNativeDevices(
            final List<? extends List<? extends AbstractDevice>> deviceLists) {
        return deviceLists.stream()
                .map(a -> a.stream()
                        .map(device -> (NativeDevice) device)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, NativeDevice> getDefaultNativeDeviceMap(final List<? extends AbstractDevice> defaultDevices) {
        return defaultDevices.stream()
                .map(device -> (NativeDevice) device)
                .collect(Collectors.toMap(AbstractDevice::getAlias, nativeDevice -> nativeDevice));
    }

    private void checkNativeCapabilities(final String configName,
                                         final List<String> envList,
                                         final List<UiConfig> uiConfigList,
                                         final List<List<NativeDevice>> nativeLists,
                                         final Map<String, NativeDevice> nativeDeviceDefaultMap) {
        for (int envNum = 0; envNum < uiConfigList.size(); envNum++) {
            for (NativeDevice nativeDevice : nativeLists.get(envNum)) {
                ConnectionType cT = uiConfigList.get(envNum).getNative().getConnection();
                NativeDevice defaultDevice = nativeDeviceDefaultMap.get(nativeDevice.getAlias());
                AppiumNativeCapabilities appiumCapabilities = defaultDevice.getAppiumCapabilities();
                checkNativeAppiumCapabilities(envNum, configName, cT, envList, nativeDevice, appiumCapabilities);
                BrowserStackNativeCapabilities bSCapabilities = defaultDevice.getBrowserStackCapabilities();
                checkNativeBrowserStackCapabilities(envNum, configName, cT, envList, nativeDevice, bSCapabilities);
            }
        }
    }

    private void checkNativeAppiumCapabilities(final int envNum,
                                               final String configName,
                                               final ConnectionType connectionType,
                                               final List<String> envList,
                                               final NativeDevice device,
                                               final AppiumNativeCapabilities defaultAppiumCapabilities) {
        if (nonNull(connectionType.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(connectionType.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkNativeBrowserStackCapabilities(final int envNum,
                                                     final String configName,
                                                     final ConnectionType connectionType,
                                                     final List<String> envList,
                                                     final NativeDevice device,
                                                     final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(connectionType.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envList, envNum));
        } else if (nonNull(connectionType.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private String getConfigPath(final List<String> envsList, final int envNum) {
        return FileSearcher.searchFileFromEnvFolder(envsList.get(envNum), UI_CONFIG_FILENAME).get().getPath()
                .replace(TestResourceSettings.getInstance().getTestResourcesFolder().getPath(), StringUtils.EMPTY);
    }

    private interface UiConfigPredicate extends Predicate<UiConfig> { }
    private interface UiConfigToBaseurl extends Function<UiConfig, String> { }
    private interface UiConfigToConnectionType extends Function<UiConfig, ConnectionType> { }
}
