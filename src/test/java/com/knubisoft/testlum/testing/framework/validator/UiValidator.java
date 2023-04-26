package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.Platform;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SAME_DEVICE_ALIASES;

public class UiValidator {

    public void validateUiConfig(final Map<String, UiConfig> uiConfigMap) {
        List<String> envList = new ArrayList<>(uiConfigMap.keySet());
        List<Native> nativeList = uiConfigMap.values().stream()
                .map(UiConfig::getNative)
                .collect(Collectors.toList());
        validateNative(envList, nativeList);
    }

    private void validateNative(final List<String> envList, final List<Native> nativeList) {
        List<List<NativeDevice>> deviceList = nativeList.stream()
                .map(a -> a.getDevices().getDevice())
                .collect(Collectors.toList());
        if (nativeList.size() > 0) {
            checkConnection(envList, nativeList);
            checkEnabledDevices(envList, deviceList);
            checkAliasesDifference(envList, deviceList);

            int numOfDevices = deviceList.stream().findFirst().get().size();
            checkAliasesSimilarity(envList, numOfDevices, deviceList);
            checkPlatformSimilarity(envList, numOfDevices, deviceList);
        }
    }

    private void checkPlatformSimilarity(final List<String> envList,
                                         final int numOfDevices,
                                         final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int envNum = 0; envNum < numOfDevices; envNum++) {
            List<String> platformList = getPlatforms(abstractDeviceList, envNum);
            if (platformList.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException("Every single <device> in <native> block must have the"
                        + " same <platformName> argument in all configs.\n"
                        + "Invalid config: " + FileSearcher.searchFileFromEnvFolder(
                        envList.get(envNum), "ui.xml").get().getPath() + "\n"
                        + "Invalid aliases: " + String.join(", ", platformList));
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

    private void checkConnection(final List<String> envs,
                                 final List<Native> nativeList) {
        List<ConnectionType> connectionTypeList = nativeList.stream()
                .map(Native::getConnection)
                .collect(Collectors.toList());
        if (connectionTypeList.stream()
                .allMatch(connectionType -> Objects.nonNull(connectionType.getAppiumServer()))) {
            checkAppiumServerUrl(envs, connectionTypeList);
        } else if (!connectionTypeList.stream()
                .allMatch(connectionType -> Objects.nonNull(connectionType.getBrowserStack()))) {
            throw new DefaultFrameworkException("Connection type in <native> block must be the same in each ui config");
        }
    }

    private void checkAppiumServerUrl(final List<String> envs,
                                      final List<ConnectionType> connectionTypeList) {
        Set<String> serverUrlList = new HashSet<>();
        for (int envNum = 0; envNum < connectionTypeList.size(); envNum++) {
            if (!serverUrlList.add(connectionTypeList.get(envNum).getAppiumServer().getServerUrl())) {
                throw new DefaultFrameworkException("Appium server url must be different for each <native> block\n"
                        + "Invalid env: " + FileSearcher.searchFileFromEnvFolder(
                        envs.get(envNum), "ui.xml").get().getPath());
            }
        }
    }

    private void checkEnabledDevices(final List<String> envs,
                                     final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        long firstEnvEnabledDevices = abstractDeviceList.stream().findFirst().get().size();
        for (int envNum = 0; envNum < abstractDeviceList.size(); envNum++) {
            long nextEnvEnabledDevices = getNumOfEnabledDevices(abstractDeviceList.get(envNum));
            if (nextEnvEnabledDevices != firstEnvEnabledDevices) {
                throw new DefaultFrameworkException(
                        "Every single <device> in <native> block must be either enabled or disabled in all configs\n"
                                + "Invalid config: " + FileSearcher.searchFileFromEnvFolder(
                                envs.get(envNum), "ui.xml").get().getPath() + "\n");
            }
        }
    }

    private long getNumOfEnabledDevices(final List<? extends AbstractDevice> abstractDeviceList) {
        return abstractDeviceList.stream()
                .filter(AbstractDevice::isEnabled)
                .count();
    }

    private void checkAliasesSimilarity(final List<String> envs,
                                        final int numOfDevices,
                                        final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int envNum = 0; envNum < numOfDevices; envNum++) {
            List<String> aliases = getAliases(abstractDeviceList, envNum);
            if (aliases.stream().distinct().count() > 1) {
                throw new DefaultFrameworkException("Every single <device> in <native> block must have the"
                        + " same <alias> argument in all configs.\n"
                        + "Invalid config: " + FileSearcher.searchFileFromEnvFolder(
                        envs.get(envNum), "ui.xml").get().getPath() + "\n"
                        + "Invalid aliases: " + String.join(", ", aliases));
            }
        }
    }

    private List<String> getAliases(final List<? extends List<? extends AbstractDevice>> abstractDeviceList,
                                    final int envNum) {
        return abstractDeviceList.stream()
                .map(integration -> integration.get(envNum))
                .filter(AbstractDevice::isEnabled)
                .map(AbstractDevice::getAlias)
                .collect(Collectors.toList());
    }

    private void checkAliasesDifference(final List<String> envsList,
                                        final List<? extends List<? extends AbstractDevice>> abstractDeviceList) {
        for (int envNum = 0; envNum < abstractDeviceList.size(); envNum++) {
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
}
