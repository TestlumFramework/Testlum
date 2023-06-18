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
import java.util.stream.IntStream;

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
import static java.util.Collections.singletonMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class UiValidator {

    private static final String DEVICE = "device";
    private static final String BROWSER = "browser";
    private final Map<String, Map<UiConfigPredicate, UiConfigToUiSettings>> uiConfigMethodMap;
    private final Map<String, Map<UiConfigPredicate, UiConfigToBaseurl>> baseUrlMethodMap;
    private final Map<String, Map<UiConfigPredicate, UiConfigToConnectionType>> connectionMethodMap;

    public UiValidator() {
        final Map<String, Map<UiConfigPredicate, UiConfigToUiSettings>> configMethodMap = new HashMap<>();
        configMethodMap.put(WEB.name(), singletonMap(c -> nonNull(c.getWeb())
                && c.getWeb().isEnabled(), UiConfig::getWeb));
        configMethodMap.put(NATIVE.name(), singletonMap(c -> nonNull(c.getNative())
                && c.getNative().isEnabled(), UiConfig::getNative));
        configMethodMap.put(MOBILE_BROWSER.name(), singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), UiConfig::getMobilebrowser));
        uiConfigMethodMap = Collections.unmodifiableMap(configMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToBaseurl>> baseUrlMethodMap = new HashMap<>();
        baseUrlMethodMap.put(WEB.name(), singletonMap(c -> nonNull(c.getWeb())
                && c.getWeb().isEnabled(), c -> c.getWeb().getBaseUrl()));
        baseUrlMethodMap.put(MOBILE_BROWSER.name(), singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), c -> c.getMobilebrowser().getBaseUrl()));
        this.baseUrlMethodMap = Collections.unmodifiableMap(baseUrlMethodMap);

        final Map<String, Map<UiConfigPredicate, UiConfigToConnectionType>> cTypeMethodMap = new HashMap<>();
        cTypeMethodMap.put(NATIVE.name(), singletonMap(c -> nonNull(c.getNative())
                && c.getNative().isEnabled(), c -> c.getNative().getConnection()));
        cTypeMethodMap.put(MOBILE_BROWSER.name(), singletonMap(c -> nonNull(c.getMobilebrowser())
                && c.getMobilebrowser().isEnabled(), c -> c.getMobilebrowser().getConnection()));
        connectionMethodMap = Collections.unmodifiableMap(cTypeMethodMap);
    }

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());
        List<UiConfig> uiConfigs = new ArrayList<>(uiConfigMap.values());
        if (!uiConfigs.isEmpty()) {
            validateNativeOrMobileOrWebPresence(envList.size(), uiConfigs);
            validateBaseUrl(envList.size(), uiConfigs);
            validateConnection(uiConfigs);
            validateDevicesAndBrowsers(envList, uiConfigs);
        }
    }

    private void validateNativeOrMobileOrWebPresence(final int envNum, final List<UiConfig> uiConfigs) {
        uiConfigMethodMap.forEach((nativeOrMobileOrWebName, uiConfigMap) ->
                uiConfigMap.forEach((nonNullCheck, uiConfigToNativeOrMobileOrWebMethod) -> {
                    List<?> nativeOrMobileOrWebList = uiConfigs.stream()
                            .filter(nonNullCheck)
                            .map(uiConfigToNativeOrMobileOrWebMethod)
                            .collect(Collectors.toList());
                    if (!nativeOrMobileOrWebList.isEmpty() && nativeOrMobileOrWebList.size() != envNum) {
                        throw new DefaultFrameworkException(UI_CONFIG_NOT_PRESENT_IN_ALL_ENVS, nativeOrMobileOrWebName);
                    }
                })
        );
    }

    private void validateBaseUrl(final int envNum, final List<UiConfig> uiConfigs) {
        baseUrlMethodMap.forEach((mobileOrWebConfigName, urlMap) ->
                urlMap.forEach((nonNullCheck, baseUrlMethod) -> {
                    List<String> baseUrlList = uiConfigs.stream()
                            .filter(nonNullCheck)
                            .map(baseUrlMethod)
                            .collect(Collectors.toList());
                    if (!baseUrlList.isEmpty() && baseUrlList.stream().distinct().count() != envNum) {
                        throw new DefaultFrameworkException(BASE_URLS_ARE_SAME, mobileOrWebConfigName);
                    }
                })
        );
    }

    private void validateConnection(final List<UiConfig> uiConfigs) {
        connectionMethodMap.forEach((mobileOrNativeConfigName, connectionTypeMap) ->
                connectionTypeMap.forEach((nonNullCheck, connectionTypeMethod) -> {
                    List<ConnectionType> cTypeList = uiConfigs.stream()
                            .filter(nonNullCheck)
                            .map(connectionTypeMethod)
                            .collect(Collectors.toList());
                    checkConnectionType(mobileOrNativeConfigName, uiConfigs, cTypeList);
                })
        );
    }

    private void checkConnectionType(final String mobileOrNativeConfigName,
                                     final List<UiConfig> uiConfigs,
                                     final List<ConnectionType> cTypeList) {
        if (cTypeList.stream().allMatch(cType -> nonNull(cType.getAppiumServer()))) {
            checkAppiumServerUrl(mobileOrNativeConfigName, cTypeList);
        } else if (cTypeList.stream().allMatch(cType -> nonNull(cType.getBrowserStack()))) {
            checkBrowserStackLogin(uiConfigs);
        } else {
            throw new DefaultFrameworkException(CONNECTION_TYPE_NOT_MATCH, mobileOrNativeConfigName);
        }
    }

    private void checkAppiumServerUrl(final String mobileOrNativeConfigName,
                                      final List<ConnectionType> cTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        cTypeList.forEach(cType -> {
            String serverUrl = cType.getAppiumServer().getServerUrl();
            if (!serverUrlList.add(serverUrl)) {
                throw new DefaultFrameworkException(SAME_APPIUM_SERVER_URLS, mobileOrNativeConfigName);
            }
        });
    }

    private void checkBrowserStackLogin(final List<UiConfig> uiConfigs) {
        List<BrowserStackLogin> browserStackLoginList = uiConfigs.stream()
                .map(UiConfig::getBrowserStackLogin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (browserStackLoginList.size() != uiConfigs.size()) {
            throw new DefaultFrameworkException(BROWSERSTACK_LOGIN_NOT_CONFIGURED);
        }
    }

    private void validateDevicesAndBrowsers(final List<String> envList, final List<UiConfig> uiConfigs) {
        uiConfigMethodMap.forEach((nativeOrMobileOrWeb, configMap) ->
                configMap.forEach((nonNullCheck, configMethod) -> {
                    List<List<?>> deviceOrBrowserLists = getDevicesOrBrowsers(uiConfigs, nonNullCheck, configMethod);
                    if (!deviceOrBrowserLists.isEmpty() && deviceOrBrowserLists.size() != envList.size()) {
                        throw new DefaultFrameworkException(ENVIRONMENT_MISSING_DEVICES_OR_BROWSERS,
                                deviceOrBrowserLists.get(0).get(0).getClass().getSimpleName(), nativeOrMobileOrWeb);
                    } else if (!deviceOrBrowserLists.isEmpty()) {
                        devicesAndBrowsersValidation(nativeOrMobileOrWeb, envList, uiConfigs, deviceOrBrowserLists);
                    }
                })
        );
    }

    private List<List<?>> getDevicesOrBrowsers(final List<UiConfig> uiConfigs,
                                               final UiConfigPredicate nonNullCheck,
                                               final UiConfigToUiSettings configMethod) {
        return uiConfigs.stream()
                .filter(nonNullCheck)
                .map(configMethod)
                .map(config -> getDeviceOrBrowserList(config).stream()
                        .filter(this::isDeviceOrBrowserEnabled)
                        .collect(Collectors.toList()))
                .filter(deviceOrBrowserList -> !deviceOrBrowserList.isEmpty())
                .collect(Collectors.toList());
    }

    private List<?> getDeviceOrBrowserList(final Object config) {
        return config instanceof Web
                ? ((Web) config).getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari()
                : config instanceof Native
                ? ((Native) config).getDevices().getDevice()
                : ((Mobilebrowser) config).getDevices().getDevice();
    }

    private boolean isDeviceOrBrowserEnabled(final Object deviceOrBrowser) {
        return deviceOrBrowser instanceof AbstractDevice
                ? ((AbstractDevice) deviceOrBrowser).isEnabled()
                : ((AbstractBrowser) deviceOrBrowser).isEnabled();
    }

    private void devicesAndBrowsersValidation(final String configName,
                                              final List<String> envList,
                                              final List<UiConfig> uiConfigs,
                                              final List<List<?>> deviceOrBrowserLists) {
        List<?> defaultDeviceOrBrowserList = deviceOrBrowserLists.stream()
                .min(Comparator.comparingInt(List::size)).get();
        if (configName.equals(WEB.name())) {
            Map<String, String> defaultBrowserMap = filterAbstractBrowser(defaultDeviceOrBrowserList).stream()
                    .collect(Collectors.toMap(AbstractBrowser::getAlias, b -> b.getClass().getSimpleName()));
            List<List<? extends AbstractBrowser>> browserLists = deviceOrBrowserLists.stream()
                    .map(this::filterAbstractBrowser)
                    .collect(Collectors.toList());
            checkBrowserAliasesDifferAndMatch(configName, envList, defaultBrowserMap, browserLists);
        } else {
            validateDevices(configName, envList, uiConfigs, defaultDeviceOrBrowserList, deviceOrBrowserLists);
        }
    }

    private List<? extends AbstractBrowser> filterAbstractBrowser(final List<?> deviceOrBrowserList) {
        return deviceOrBrowserList.stream()
                .filter(deviceOrBrowser -> deviceOrBrowser instanceof AbstractBrowser)
                .map(AbstractBrowser.class::cast)
                .collect(Collectors.toList());
    }

    private void checkBrowserAliasesDifferAndMatch(final String configName,
                                                   final List<String> envList,
                                                   final Map<String, String> defaultBrowserMap,
                                                   final List<List<? extends AbstractBrowser>> browserLists) {
        IntStream.range(0, envList.size()).forEach(envNum -> {
            Set<String> aliasSet = new HashSet<>();
            browserLists.get(envNum).forEach(browser -> {
                if (!aliasSet.add(browser.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_DIFFER, BROWSER,
                            configName, browser.getAlias(), getConfigPath(envList.get(envNum)));
                } else if (!defaultBrowserMap.containsKey(browser.getAlias())
                        || !defaultBrowserMap.get(browser.getAlias()).equals(browser.getClass().getSimpleName())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_MATCH, browser.getClass().getSimpleName(),
                            BROWSER, configName, browser.getAlias());
                }
            });
        });
    }

    private void validateDevices(final String configName,
                                 final List<String> envList,
                                 final List<UiConfig> uiConfigList,
                                 final List<?> defaultDeviceOrBrowserList,
                                 final List<List<?>> deviceOrBrowserLists) {
        List<? extends AbstractDevice> defaultDevices = filterAbstractDevices(defaultDeviceOrBrowserList);
        List<List<? extends AbstractDevice>> deviceLists = deviceOrBrowserLists.stream()
                .map(this::filterAbstractDevices)
                .collect(Collectors.toList());
        checkDeviceAliasesDifferAndMatch(configName, envList, defaultDevices, deviceLists);
        checkPlatformNameMatch(configName, defaultDevices, deviceLists);
        validateDeviceCapabilities(configName, envList, uiConfigList, defaultDevices, deviceLists);
    }

    private List<? extends AbstractDevice> filterAbstractDevices(final List<?> deviceOrBrowserList) {
        return deviceOrBrowserList.stream()
                .filter(deviceOrBrowser -> deviceOrBrowser instanceof AbstractDevice)
                .map(AbstractDevice.class::cast)
                .collect(Collectors.toList());
    }

    private void checkDeviceAliasesDifferAndMatch(final String configName,
                                                  final List<String> envList,
                                                  final List<? extends AbstractDevice> defaultDevices,
                                                  final List<List<? extends AbstractDevice>> deviceLists) {
        List<String> defaultAliasList = defaultDevices.stream()
                .map(AbstractDevice::getAlias).collect(Collectors.toList());
        IntStream.range(0, envList.size()).forEach(envNum -> {
            Set<String> aliasSet = new HashSet<>();
            deviceLists.get(envNum).forEach(device -> {
                if (!aliasSet.add(device.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_DIFFER, DEVICE,
                            configName, device.getAlias(), getConfigPath(envList.get(envNum)));
                } else if (!defaultAliasList.contains(device.getAlias())) {
                    throw new DefaultFrameworkException(UI_ALIASES_NOT_MATCH, device.getClass().getSimpleName(),
                            DEVICE, configName, device.getAlias());
                }
            });
        });
    }

    private void checkPlatformNameMatch(final String configName,
                                        final List<? extends AbstractDevice> defaultDevicesList,
                                        final List<List<? extends AbstractDevice>> deviceLists) {
        deviceLists.forEach(devices -> devices.forEach(device -> {
            boolean platformNameNoneMatch = defaultDevicesList.stream()
                    .filter(d -> device.getAlias().equals(d.getAlias()))
                    .anyMatch(d -> !d.getPlatformName().value().equals(device.getPlatformName().value()));
            if (platformNameNoneMatch) {
                throw new DefaultFrameworkException(DEVICE_PLATFORMS_NOT_MATCH, configName, device.getAlias());
            }
        }));
    }

    private void validateDeviceCapabilities(final String configName,
                                            final List<String> envList,
                                            final List<UiConfig> uiConfigs,
                                            final List<? extends AbstractDevice> defaultDeviceList,
                                            final List<List<? extends AbstractDevice>> deviceLists) {
        if (configName.equals(MOBILE_BROWSER.name())) {
            List<List<MobilebrowserDevice>> mobilebrowserDevices = getAllMobilebrowserDevices(deviceLists);
            Map<String, MobilebrowserDevice> defaultMobileDeviceMap = getMobilebrowserDeviceMap(defaultDeviceList);
            checkMobilebrowserCapabilities(configName, envList, uiConfigs,
                    mobilebrowserDevices, defaultMobileDeviceMap);
        }
        if (configName.equals(NATIVE.name())) {
            List<List<NativeDevice>> nativeDevices = getAllNativeDevices(deviceLists);
            Map<String, NativeDevice> defaultNativeDeviceMap = getDefaultNativeDeviceMap(defaultDeviceList);
            checkNativeCapabilities(configName, envList, uiConfigs, nativeDevices, defaultNativeDeviceMap);
        }
    }

    private List<List<MobilebrowserDevice>> getAllMobilebrowserDevices(
            final List<List<? extends AbstractDevice>> deviceLists) {
        return deviceLists.stream()
                .map(devices -> devices.stream()
                        .filter(device -> device instanceof MobilebrowserDevice)
                        .map(MobilebrowserDevice.class::cast).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, MobilebrowserDevice> getMobilebrowserDeviceMap(
            final List<? extends AbstractDevice> defaultDevices) {
        return defaultDevices.stream()
                .filter(device -> device instanceof MobilebrowserDevice)
                .map(MobilebrowserDevice.class::cast)
                .collect(Collectors.toMap(AbstractDevice::getAlias, Function.identity()));
    }

    private void checkMobilebrowserCapabilities(final String configName,
                                                final List<String> envList,
                                                final List<UiConfig> uiConfigs,
                                                final List<List<MobilebrowserDevice>> mobilebrowserLists,
                                                final Map<String, MobilebrowserDevice> mobilebrowserDeviceDefaultMap) {
        IntStream.range(0, uiConfigs.size()).forEach(envNum ->
                mobilebrowserLists.get(envNum).forEach(mobileDevice -> {
                    ConnectionType cType = uiConfigs.get(envNum).getMobilebrowser().getConnection();
                    MobilebrowserDevice defaultDevice = mobilebrowserDeviceDefaultMap.get(mobileDevice.getAlias());
                    checkMobilebrowserAppiumCapabilities(envList.get(envNum), configName, cType, mobileDevice,
                            defaultDevice.getAppiumCapabilities());
                    checkMobilebrowserBrowserStackCapapabilities(envList.get(envNum), configName, cType, mobileDevice,
                            defaultDevice.getBrowserStackCapabilities());
                })
        );
    }

    private void checkMobilebrowserAppiumCapabilities(final String envName,
                                                      final String configName,
                                                      final ConnectionType cType,
                                                      final MobilebrowserDevice device,
                                                      final AppiumCapabilities defaultAppiumCapabilities) {
        if (nonNull(cType.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envName));
        } else if (nonNull(cType.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkMobilebrowserBrowserStackCapapabilities(final String envName,
                                                              final String configName,
                                                              final ConnectionType cType,
                                                              final MobilebrowserDevice device,
                                                              final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(cType.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envName));
        } else if (nonNull(cType.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private List<List<NativeDevice>> getAllNativeDevices(final List<List<? extends AbstractDevice>> deviceLists) {
        return deviceLists.stream()
                .map(devices -> devices.stream()
                        .filter(device -> device instanceof NativeDevice)
                        .map(NativeDevice.class::cast)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private Map<String, NativeDevice> getDefaultNativeDeviceMap(final List<? extends AbstractDevice> defaultDevices) {
        return defaultDevices.stream()
                .filter(device -> device instanceof NativeDevice)
                .map(NativeDevice.class::cast)
                .collect(Collectors.toMap(AbstractDevice::getAlias, nativeDevice -> nativeDevice));
    }

    private void checkNativeCapabilities(final String configName,
                                         final List<String> envList,
                                         final List<UiConfig> uiConfigs,
                                         final List<List<NativeDevice>> nativeLists,
                                         final Map<String, NativeDevice> nativeDeviceDefaultMap) {
        IntStream.range(0, uiConfigs.size()).forEach(envNum ->
                nativeLists.get(envNum).forEach(nativeDevice -> {
                    ConnectionType cType = uiConfigs.get(envNum).getNative().getConnection();
                    NativeDevice defaultDevice = nativeDeviceDefaultMap.get(nativeDevice.getAlias());
                    checkNativeAppiumCapabilities(envList.get(envNum), configName, cType, nativeDevice,
                            defaultDevice.getAppiumCapabilities());
                    checkNativeBrowserStackCapabilities(envList.get(envNum), configName, cType, nativeDevice,
                            defaultDevice.getBrowserStackCapabilities());
                })
        );
    }

    private void checkNativeAppiumCapabilities(final String envName,
                                               final String configName,
                                               final ConnectionType cType,
                                               final NativeDevice device,
                                               final AppiumNativeCapabilities defaultAppiumCapabilities) {
        if (nonNull(cType.getAppiumServer()) && Objects.isNull(device.getAppiumCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envName));
        } else if (nonNull(cType.getAppiumServer())
                && (isNull(device.getAppiumCapabilities()) || isNull(defaultAppiumCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private void checkNativeBrowserStackCapabilities(final String envName,
                                                     final String configName,
                                                     final ConnectionType cType,
                                                     final NativeDevice device,
                                                     final BrowserStackCapabilities defaultBSCapabilities) {
        if (nonNull(cType.getBrowserStack()) && Objects.isNull(device.getBrowserStackCapabilities())) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE,
                    device.getAlias(), configName, getConfigPath(envName));
        } else if (nonNull(cType.getBrowserStack())
                && (isNull(device.getBrowserStackCapabilities()) || isNull(defaultBSCapabilities))) {
            throw new DefaultFrameworkException(CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS, device.getAlias(), configName);
        }
    }

    private String getConfigPath(final String envName) {
        return FileSearcher.searchFileFromEnvFolder(envName, UI_CONFIG_FILENAME).get().getPath()
                .replace(TestResourceSettings.getInstance().getTestResourcesFolder().getPath(), StringUtils.EMPTY);
    }

    private interface UiConfigPredicate extends Predicate<UiConfig> { }
    private interface UiConfigToUiSettings extends Function<UiConfig, Object> { }
    private interface UiConfigToBaseurl extends Function<UiConfig, String> { }
    private interface UiConfigToConnectionType extends Function<UiConfig, ConnectionType> { }
}
