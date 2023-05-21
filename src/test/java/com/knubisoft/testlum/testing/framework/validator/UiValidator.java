package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.BrowserStackLogin;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings.UI_CONFIG_FILENAME;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ALIASES_NOT_MATCH_IN_UI_CONFIG;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.BROWSERSTACK_LOGIN_NOT_CONFIGURED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DIFFERENT_BROWSER_ALIASES;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CONNECTION_TYPE_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.MOBILEBROWSER_NUM_NOT_MATCH_ENVS_NUM;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NATIVE_NUM_NOT_MATCH_ENVS_NUM;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NUM_OF_ENABLED_BROWSERS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NUM_OF_ENABLED_DEVICES_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.PLATFORMS_NOT_MATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_APPIUM_SERVER_URLS;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_BROWSER_ALIASES;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_DEVICE_ALIASES;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.WEB_NUM_NOT_MATCH_ENVS_NUM;
import static java.util.Objects.nonNull;

public class UiValidator {

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());

        validateWeb(envList, new ArrayList<>(uiConfigMap.values()));

        List<Native> nativeList = getEnabledNativeList(new ArrayList<>(uiConfigMap.values()));
        validateNative(envList, nativeList, new ArrayList<>(uiConfigMap.values()));

        List<Mobilebrowser> mobilebrowserList = getEnabledMobilebrowserList(new ArrayList<>(uiConfigMap.values()));
        validateMobileBrowser(envList, mobilebrowserList, new ArrayList<>(uiConfigMap.values()));
    }

    private List<Native> getEnabledNativeList(final List<UiConfig> uiConfigList) {
        return uiConfigList.stream()
                .map(UiConfig::getNative)
                .filter(Objects::nonNull)
                .filter(Native::isEnabled)
                .collect(Collectors.toList());
    }

    private List<Mobilebrowser> getEnabledMobilebrowserList(final List<UiConfig> uiConfigList) {
        return uiConfigList.stream()
                .map(UiConfig::getMobilebrowser)
                .filter(Objects::nonNull)
                .filter(Mobilebrowser::isEnabled)
                .collect(Collectors.toList());
    }

    private void validateWeb(final List<String> envList,
                             final List<UiConfig> uiConfigList) {
        List<Web> webList = uiConfigList.stream()
                .map(UiConfig::getWeb)
                .filter(Objects::nonNull)
                .filter(Web::isEnabled)
                .collect(Collectors.toList());
        if (!webList.isEmpty() && webList.size() != envList.size()) {
            throw new DefaultFrameworkException(WEB_NUM_NOT_MATCH_ENVS_NUM);
        } else if (!webList.isEmpty()) {
            List<List<AbstractBrowser>> browserList = webList.stream()
                    .map(webConfig -> webConfig.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari())
                    .collect(Collectors.toList());
            validateBrowsers(envList, browserList);
        }
    }

    private void validateBrowsers(final List<String> envList,
                                  final List<List<AbstractBrowser>> browserList) {
        int numOfEnabledDevices = getNumOfEnabledBrowsers(browserList.stream().findFirst().get());
        checkNumberOfEnabledBrowsers(envList, numOfEnabledDevices, browserList);
        checkBrowserAliasesDifference(envList, browserList);
        if (envList.size() > 1) {
            checkBrowserAliasesSimilarity(numOfEnabledDevices, browserList);
        }
    }

    private void checkBrowserAliasesSimilarity(final int numOfEnabledDevices,
                                               final List<? extends List<? extends AbstractBrowser>> browserList) {
        for (int deviceNum = 0; deviceNum < numOfEnabledDevices; deviceNum++) {
            List<String> aliases = getBrowserAliases(browserList, deviceNum);
            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException(DIFFERENT_BROWSER_ALIASES,
                        UI_CONFIG_FILENAME, String.join(", ", aliases));
            }
        }
    }

    private List<String> getBrowserAliases(final List<? extends List<? extends AbstractBrowser>> abstractDeviceList,
                                           final int envNum) {
        return abstractDeviceList.stream()
                .map(deviceList -> deviceList.get(envNum))
                .map(AbstractBrowser::getAlias)
                .collect(Collectors.toList());
    }

    private void checkBrowserAliasesDifference(final List<String> envsList,
                                               final List<? extends List<? extends AbstractBrowser>> browserList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (AbstractBrowser abstractBrowser : browserList.get(envNum)) {
                if (!aliasSet.add(abstractBrowser.getAlias())) {
                    throw new DefaultFrameworkException(SAME_BROWSER_ALIASES, abstractBrowser.getAlias(),
                            FileSearcher.searchFileFromEnvFolder(
                                    envsList.get(envNum), UI_CONFIG_FILENAME).get().getPath());
                }
            }
        }
    }

    private void checkNumberOfEnabledBrowsers(final List<String> envsList,
                                              final int firstEnvEnabledDevices,
                                              final List<? extends List<? extends AbstractBrowser>> deviceList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            int nexEnvEnabledBrowsers = this.getNumOfEnabledBrowsers(deviceList.get(envNum));
            if (nexEnvEnabledBrowsers != firstEnvEnabledDevices) {
                throw new DefaultFrameworkException(NUM_OF_ENABLED_BROWSERS_NOT_MATCH);
            }
        }
    }

    private int getNumOfEnabledBrowsers(final List<? extends AbstractBrowser> devices) {
        return devices.stream()
                .filter(AbstractBrowser::isEnabled)
                .mapToInt(e -> 1).sum();
    }

    private void validateNative(final List<String> envList,
                                final List<Native> nativeList,
                                final List<UiConfig> uiConfigList) {
        if (!nativeList.isEmpty() && nativeList.size() != envList.size()) {
            throw new DefaultFrameworkException(NATIVE_NUM_NOT_MATCH_ENVS_NUM);
        } else if (!nativeList.isEmpty()) {
            String nativeName = nativeList.stream().findFirst().get().getClass().getSimpleName();
            List<ConnectionType> connectionTypeList = nativeList.stream()
                    .map(Native::getConnection)
                    .collect(Collectors.toList());
            checkConnection(nativeName, connectionTypeList, uiConfigList);
            List<List<NativeDevice>> deviceList = nativeList.stream()
                    .map(nativeConfig -> nativeConfig.getDevices().getDevice())
                    .collect(Collectors.toList());
            validateDevices(nativeName, envList, deviceList);
        }
    }

    private void validateMobileBrowser(final List<String> envList,
                                       final List<Mobilebrowser> mobilebrowserList,
                                       final List<UiConfig> uiConfigList) {
        if (!mobilebrowserList.isEmpty() && mobilebrowserList.size() != envList.size()) {
            throw new DefaultFrameworkException(MOBILEBROWSER_NUM_NOT_MATCH_ENVS_NUM);
        } else if (!mobilebrowserList.isEmpty()) {
            String mobilebrowserName = mobilebrowserList.stream().findFirst().get().getClass().getSimpleName();
            List<ConnectionType> connectionTypeList = mobilebrowserList.stream()
                    .map(Mobilebrowser::getConnection)
                    .collect(Collectors.toList());
            checkConnection(mobilebrowserName, connectionTypeList, uiConfigList);
            List<List<MobilebrowserDevice>> deviceList = mobilebrowserList.stream()
                    .map(mobilebrowserConfig -> mobilebrowserConfig.getDevices().getDevice())
                    .collect(Collectors.toList());
            validateDevices(mobilebrowserName, envList, deviceList);
        }
    }

    private void checkConnection(final String configName,
                                 final List<ConnectionType> connectionTypeList,
                                 final List<UiConfig> uiConfigList) {
        if (connectionTypeList.stream()
                .allMatch(connectionType -> nonNull(connectionType.getAppiumServer()))) {
            checkAppiumServerUrl(configName, connectionTypeList);
        } else if (connectionTypeList.stream()
                .allMatch(connectionType -> nonNull(connectionType.getBrowserStack()))) {
            checkBrowserStackLogin(configName, uiConfigList);
        } else {
            throw new DefaultFrameworkException(CONNECTION_TYPE_NOT_MATCH, configName, UI_CONFIG_FILENAME);
        }
    }

    private void checkAppiumServerUrl(final String configName,
                                      final List<ConnectionType> connectionTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        for (ConnectionType connectionType : connectionTypeList) {
            String serverUrl = connectionType.getAppiumServer().getServerUrl();
            if (!serverUrlList.add(serverUrl)) {
                throw new DefaultFrameworkException(SAME_APPIUM_SERVER_URLS, configName, UI_CONFIG_FILENAME);
            }
        }
    }

    private void checkBrowserStackLogin(final String configName,
                                        final List<UiConfig> uiConfigList) {
        List<BrowserStackLogin> browserStackLoginList = uiConfigList.stream()
                .map(UiConfig::getBrowserStackLogin)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (browserStackLoginList.size() == uiConfigList.size()) {
            throw new DefaultFrameworkException(BROWSERSTACK_LOGIN_NOT_CONFIGURED, configName, UI_CONFIG_FILENAME);
        }
    }

    private void validateDevices(final String configName,
                                 final List<String> envList,
                                 final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        checkNumberOfEnabledDevices(configName, envList, abstractDeviceList);
        checkDeviceAliasesDifference(configName, envList, abstractDeviceList);
        if (envList.size() > 1) {
            for (int deviceNum = 0; deviceNum < getNumOfEnabledDevices(abstractDeviceList.get(0)); deviceNum++) {
                checkDeviceAliasesSimilarity(configName, deviceNum, abstractDeviceList);
                checkPlatformSimilarity(configName, deviceNum, abstractDeviceList);
            }
        }
    }

    private void checkNumberOfEnabledDevices(final String configName,
                                             final List<String> envsList,
                                             final List<? extends List<? extends AbstractDevice>> deviceList) {
        int defaultNumOfEnabledDevices = -1;
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            if (defaultNumOfEnabledDevices == -1) {
                defaultNumOfEnabledDevices = getNumOfEnabledDevices(deviceList.get(envNum));
            }
            if (defaultNumOfEnabledDevices != getNumOfEnabledDevices(deviceList.get(envNum))) {
                throw new DefaultFrameworkException(NUM_OF_ENABLED_DEVICES_NOT_MATCH, configName, UI_CONFIG_FILENAME);
            }
        }
    }

    private int getNumOfEnabledDevices(final List<? extends AbstractDevice> devices) {
        return devices.stream()
                .filter(AbstractDevice::isEnabled)
                .mapToInt(device -> 1).sum();
    }

    private void checkDeviceAliasesDifference(final String configName,
                                              final List<String> envsList,
                                              final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (AbstractDevice abstractDevice : abstractDeviceList.get(envNum)) {
                if (!aliasSet.add(abstractDevice.getAlias())) {
                    throw new DefaultFrameworkException(SAME_DEVICE_ALIASES, configName, abstractDevice.getAlias(),
                            FileSearcher.searchFileFromEnvFolder(
                                    envsList.get(envNum), UI_CONFIG_FILENAME).get().getPath());
                }
            }
        }
    }

    private void checkDeviceAliasesSimilarity(final String configName,
                                              final int deviceNum,
                                              final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        List<String> aliases = abstractDeviceList.stream()
                .map(deviceList -> deviceList.get(deviceNum))
                .map(AbstractDevice::getAlias)
                .collect(Collectors.toList());
        if (aliases.stream().distinct().count() > 1) {
            throw new DefaultFrameworkException(ALIASES_NOT_MATCH_IN_UI_CONFIG, configName, UI_CONFIG_FILENAME,
                    String.join(", ", aliases));
        }
    }

    private void checkPlatformSimilarity(final String configName,
                                         final int deviceNum,
                                         final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        List<String> platformList = abstractDeviceList.stream()
                .map(deviceList -> deviceList.get(deviceNum))
                .map(abstractDevice -> abstractDevice.getPlatformName().value())
                .collect(Collectors.toList());
        if (platformList.stream().distinct().count() > 1) {
            throw new DefaultFrameworkException(PLATFORMS_NOT_MATCH, configName, UI_CONFIG_FILENAME);
        }
    }
}
