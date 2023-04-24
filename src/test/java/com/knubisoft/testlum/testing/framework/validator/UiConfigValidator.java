package com.knubisoft.testlum.testing.framework.validator;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.AbstractCapabilities;
import com.knubisoft.testlum.testing.model.global_config.AbstractDevice;
import com.knubisoft.testlum.testing.model.global_config.ConnectionType;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.NativeDevice;
import com.knubisoft.testlum.testing.model.global_config.Settings;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class UiConfigValidator implements XMLValidator<UiConfig> {

    private final Map<ConnectionDevice, DeviceToCapabilities> connectionToDeviceCapabilities;

    public UiConfigValidator() {
        Map<ConnectionDevice, DeviceToCapabilities> capabilitiesMap = new HashMap<>();
        capabilitiesMap.put((c, d) -> nonNull(c.getAppiumServer()) && MobilebrowserDevice.class.equals(d),
                d -> ((MobilebrowserDevice) d).getAppiumCapabilities());
        capabilitiesMap.put((c, d) -> nonNull(c.getBrowserStack()) && MobilebrowserDevice.class.equals(d),
                d -> ((MobilebrowserDevice) d).getBrowserStackCapabilities());
        capabilitiesMap.put((c, d) -> nonNull(c.getAppiumServer()) && NativeDevice.class.equals(d),
                d -> ((NativeDevice) d).getAppiumCapabilities());
        capabilitiesMap.put((c, d) -> nonNull(c.getBrowserStack()) && NativeDevice.class.equals(d),
                d -> ((NativeDevice) d).getBrowserStackCapabilities());
        this.connectionToDeviceCapabilities = Collections.unmodifiableMap(capabilitiesMap);
    }

    @Override
    public void validate(final UiConfig uiConfig, final File xmlFile) {
        validateCapabilitiesToConnection(uiConfig, xmlFile);
    }

    private void validateCapabilitiesToConnection(final UiConfig uiConfig, final File xmlFile) {
        validateMobilebrowserSettings(uiConfig.getMobilebrowser(), xmlFile);
        validateNativeSettings(uiConfig.getNative(), xmlFile);
    }

    private void validateMobilebrowserSettings(final Mobilebrowser settings, final File xmlFile) {
        if (nonNull(settings)) {
            checkConnectionType(settings.getConnection(), settings, xmlFile);
            for (MobilebrowserDevice device : settings.getDevices().getDevice()) {
                checkConnectionCapabilities(settings.getConnection(), device, settings, xmlFile);
            }
        }
    }

    private void validateNativeSettings(final Native settings, final File xmlFile) {
        if (nonNull(settings)) {
            checkConnectionType(settings.getConnection(), settings, xmlFile);
            for (NativeDevice device : settings.getDevices().getDevice()) {
                checkConnectionCapabilities(settings.getConnection(), device, settings, xmlFile);
            }
        }
    }

    private void checkConnectionType(final ConnectionType connectionType,
                                     final Settings settings,
                                     final File xmlFile) {
        if (isNull(connectionType)) {
            throw new DefaultFrameworkException(ExceptionMessage.CONNECTION_TYPE_NOT_DEFINED,
                    settings.getClass().getSimpleName(), xmlFile.getPath());
        }
    }

    private void checkConnectionCapabilities(final ConnectionType connectionType,
                                             final AbstractDevice device,
                                             final Settings settings,
                                             final File xmlFile) {
        AbstractCapabilities capabilities = getAppropriateCapabilitiesByConnection(device, connectionType);
        if (isNull(capabilities)) {
            String configPath = xmlFile.getPath().replace(TestResourceSettings.getInstance()
                    .getTestResourcesFolder().getPath(), StringUtils.EMPTY);
            throw new DefaultFrameworkException(ExceptionMessage.DEVICE_CONNECTION_CAPABILITIES_NOT_DEFINED,
                    device.getAlias(), settings.getClass().getSimpleName(), configPath);
        }
    }

    private AbstractCapabilities getAppropriateCapabilitiesByConnection(final AbstractDevice device,
                                                                        final ConnectionType connectionType) {
        return connectionToDeviceCapabilities.entrySet().stream()
                .filter(e -> e.getKey().test(connectionType, device.getClass()))
                .findFirst()
                .map(e -> e.getValue().apply(device)).orElse(null);
    }

    private interface ConnectionDevice extends BiPredicate<ConnectionType, Class<? extends AbstractDevice>> { }
    private interface DeviceToCapabilities extends Function<AbstractDevice, AbstractCapabilities> { }
}
