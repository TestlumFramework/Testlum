package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.AbstractBrowser;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.Platform;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_BROWSER_ALIASES;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_DEVICE_ALIASES;
import static java.util.Objects.nonNull;

public class UiValidator {

    private static final String UI_CONFIG_FILE_NAME = "ui.xml";
    private static final String WEB_NUM_NOT_MATCH_WITH_ENVS_NUM = "Number of enabled <native> blocks does not match with"
            + " number of enabled envs";
    private static final String NATIVE_NUM_NOT_MATCH_WITH_ENVS_NUM = "Number of enabled <native> blocks does not match "
            + "with number of enabled envs";
    private static final String MOBILEBROWSER_NUM_NOT_MATCH_WITH_ENVS_NUM = "Number of enabled <native> blocks does not "
            + "match with number of enabled envs";

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());
        validateWeb(envList, new ArrayList<>(uiConfigMap.values()));
        validateNative(envList, new ArrayList<>(uiConfigMap.values()));
        validateMobileBrowser(envList, new ArrayList<>(uiConfigMap.values()));
    }

    private void validateWeb(final List<String> envList,
                             final List<UiConfig> uiConfigList) {
        List<Web> enabledNativeList = uiConfigList.stream()
                .map(UiConfig::getWeb)
                .filter(Objects::nonNull)
                .filter(Web::isEnabled)
                .collect(Collectors.toList());
        if (!enabledNativeList.isEmpty() && enabledNativeList.size() != envList.size()) {
            throw new DefaultFrameworkException(WEB_NUM_NOT_MATCH_WITH_ENVS_NUM);
        } else if (!enabledNativeList.isEmpty()) {
            List<List<AbstractBrowser>> browserList = enabledNativeList.stream()
                    .map(webConfig -> webConfig.getBrowserSettings().getBrowsers().getChromeOrFirefoxOrSafari())
                    .collect(Collectors.toList());
            validateBrowsers(envList, browserList);
        }
    }

    private void validateNative(final List<String> envList, final List<UiConfig> uiConfigList) {
        List<Native> enabledNativeList = uiConfigList.stream()
                .map(UiConfig::getNative)
                .filter(Objects::nonNull)
                .filter(Native::isEnabled)
                .collect(Collectors.toList());
        if (!enabledNativeList.isEmpty() && enabledNativeList.size() != envList.size()) {
            throw new DefaultFrameworkException(NATIVE_NUM_NOT_MATCH_WITH_ENVS_NUM);
        } else if (!enabledNativeList.isEmpty()) {
            List<List<NativeDevice>> deviceList = enabledNativeList.stream()
                    .map(nativeConfig -> nativeConfig.getDevices().getDevice())
                    .collect(Collectors.toList());
            checkNativeConnection(envList, enabledNativeList);
            validateDevices(envList, deviceList);
        }
    }

    private void validateMobileBrowser(final List<String> envList, final List<UiConfig> uiConfigList) {
        List<Mobilebrowser> enabledMobilebrowserList = uiConfigList.stream()
                .map(UiConfig::getMobilebrowser)
                .filter(Objects::nonNull)
                .filter(Mobilebrowser::isEnabled)
                .collect(Collectors.toList());
        if (!enabledMobilebrowserList.isEmpty() && enabledMobilebrowserList.size() != envList.size()) {
            throw new DefaultFrameworkException(MOBILEBROWSER_NUM_NOT_MATCH_WITH_ENVS_NUM);
        } else if (!enabledMobilebrowserList.isEmpty()) {
            List<List<MobilebrowserDevice>> deviceList = enabledMobilebrowserList.stream()
                    .map(mobilebrowserConfig -> mobilebrowserConfig.getDevices().getDevice())
                    .collect(Collectors.toList());
            checkMobilebrowserConnection(envList, enabledMobilebrowserList);
            validateDevices(envList, deviceList);
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
                throw new DefaultFrameworkException("Every single <device> in <native> block must have the"
                        + " same <alias> argument in all configs.\n"
                        + "Invalid aliases: " + String.join(", ", aliases));
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
                                    envsList.get(envNum), "ui.xml").get().getPath());
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
                throw new DefaultFrameworkException("Num of enabled web browsers doesnt match in config by path: "
                        + FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                        UI_CONFIG_FILE_NAME).get().getPath());
            }
        }
    }

    private int getNumOfEnabledBrowsers(final List<? extends AbstractBrowser> devices) {
        return devices.stream()
                .filter(AbstractBrowser::isEnabled)
                .mapToInt(e -> 1).sum();
    }

    private void checkNativeConnection(final List<String> envs,
                                       final List<Native> nativeList) {
        if (envs.size() > 1) {
            List<ConnectionType> connectionTypeList = nativeList.stream()
                    .map(Native::getConnection)
                    .collect(Collectors.toList());
            if (connectionTypeList.stream()
                    .allMatch(connectionType -> nonNull(connectionType.getAppiumServer()))) {
                checkAppiumServerUrl(envs, connectionTypeList);
            } else if (!connectionTypeList.stream()
                    .allMatch(connectionType -> nonNull(connectionType.getBrowserStack()))) {
                throw new DefaultFrameworkException(
                        "Connection type in <native> block must be the same in all ui configs");
            }
        }
    }

    private void checkMobilebrowserConnection(final List<String> envs,
                                              final List<Mobilebrowser> nativeList) {
        if (envs.size() > 1) {
            List<ConnectionType> connectionTypeList = nativeList.stream()
                    .map(Mobilebrowser::getConnection)
                    .collect(Collectors.toList());
            if (connectionTypeList.stream()
                    .allMatch(connectionType -> nonNull(connectionType.getAppiumServer()))) {
                checkAppiumServerUrl(envs, connectionTypeList);
            } else if (!connectionTypeList.stream()
                    .allMatch(connectionType -> nonNull(connectionType.getBrowserStack()))) {
                throw new DefaultFrameworkException(
                        "Connection type in <mobilebrowser> block must be the same in all ui configs");
            }
        }
    }

    private void checkAppiumServerUrl(final List<String> envs,
                                      final List<ConnectionType> connectionTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        for (int envNum = 0; envNum < connectionTypeList.size(); envNum++) {
            String serverUrl = connectionTypeList.get(envNum).getAppiumServer().getServerUrl();
            if (!serverUrlList.add(serverUrl)) {
                throw new DefaultFrameworkException("Appium server url must be different for each <native> block\n"
                        + "Invalid env: " + FileSearcher.searchFileFromEnvFolder(envs.get(envNum),
                        UI_CONFIG_FILE_NAME).get().getPath());
            }
        }
    }

    private void validateDevices(final List<String> envList,
                                 final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        int numOfEnabledDevices = getNumOfEnabledDevices(abstractDeviceList.stream().findFirst().get());
        checkNumberOfEnabledDevices(envList, numOfEnabledDevices, abstractDeviceList);
        checkDeviceAliasesDifference(envList, abstractDeviceList);
        if (envList.size() > 1) {
            checkDeviceAliasesSimilarity(numOfEnabledDevices, abstractDeviceList);
            checkPlatformSimilarity(numOfEnabledDevices, abstractDeviceList);
        }
    }

    private void checkNumberOfEnabledDevices(final List<String> envsList,
                                             final int firstEnvEnabledDevices,
                                             final List<? extends List<? extends AbstractDevice>> deviceList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            int nextEnvEnabledDevices = this.getNumOfEnabledDevices(deviceList.get(envNum));
            if (nextEnvEnabledDevices != firstEnvEnabledDevices) {
                throw new DefaultFrameworkException("Num of enabled native devices doesnt match in config by path: "
                        + FileSearcher.searchFileFromEnvFolder(envsList.get(envNum),
                        UI_CONFIG_FILE_NAME).get().getPath());
            }
        }
    }

    private int getNumOfEnabledDevices(final List<? extends AbstractDevice> devices) {
        return devices.stream()
                .filter(AbstractDevice::isEnabled)
                .mapToInt(e -> 1).sum();
    }

    private void checkDeviceAliasesDifference(final List<String> envsList,
                                              final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int envNum = 0; envNum < envsList.size(); envNum++) {
            Set<String> aliasSet = new HashSet<>();
            for (AbstractDevice abstractDevice : abstractDeviceList.get(envNum)) {
                if (!aliasSet.add(abstractDevice.getAlias())) {
                    throw new DefaultFrameworkException(SAME_DEVICE_ALIASES, abstractDevice.getAlias(),
                            FileSearcher.searchFileFromEnvFolder(
                                    envsList.get(envNum), "ui.xml").get().getPath());
                }
            }
        }
    }

    private void checkDeviceAliasesSimilarity(final int numOfEnabledDevices,
                                              final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int deviceNum = 0; deviceNum < numOfEnabledDevices; deviceNum++) {
            List<String> aliases = getDeviceAliases(abstractDeviceList, deviceNum);
            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException("Every single <device> in <native> block must have the"
                        + " same <alias> argument in all configs.\n"
                        + "Invalid aliases: " + String.join(", ", aliases));
            }
        }
    }

    private List<String> getDeviceAliases(final List<? extends List<? extends AbstractDevice>> abstractDeviceList,
                                          final int envNum) {
        return abstractDeviceList.stream()
                .map(deviceList -> deviceList.get(envNum))
                .map(AbstractDevice::getAlias)
                .collect(Collectors.toList());
    }

    private void checkPlatformSimilarity(final int numOfEnabledDevices,
                                         final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int deviceNum = 0; deviceNum < numOfEnabledDevices; deviceNum++) {
            List<String> platformList = getPlatforms(abstractDeviceList, deviceNum);
            if (platformList.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException("Every single <device> in <native> block must have the"
                        + " same <platformName> argument in all configs.\n");
            }
        }
    }

    private List<String> getPlatforms(final List<? extends List<? extends AbstractDevice>> abstractDeviceList,
                                      final int envNum) {
        return abstractDeviceList.stream()
                .map(integration -> integration.get(envNum))
                .filter(AbstractDevice::isEnabled)
                .map(AbstractDevice::getPlatformName)
                .map(Platform::value)
                .collect(Collectors.toList());
    }
}
