package com.knubisoft.cott.testing.framework.validator;

import com.knubisoft.cott.testing.framework.exception.AbsentCapabilityException;
import com.knubisoft.cott.testing.framework.exception.AbsentConnectionException;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.InvalidCapabilitiesToConnectionException;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.model.global_config.AbstractDevice;
import com.knubisoft.cott.testing.model.global_config.ConnectionType;
import com.knubisoft.cott.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.cott.testing.model.global_config.Mobilebrowser;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.Native;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GlobalTestConfigValidator implements XMLValidator<GlobalTestConfiguration> {

    private final SubscriptionValidator subscriptionValidator = new StripeValidationService();

    @Override
    public void validate(final GlobalTestConfiguration globalTestConfig, final File xmlFile) {
        checkIsActiveSubscription(globalTestConfig);
        checkCapabilitiesToConnection(globalTestConfig);
    }

    private void checkIsActiveSubscription(final GlobalTestConfiguration globalTestConfig) {
        if (Objects.isNull(globalTestConfig.getSubscription())) {
            throw new DefaultFrameworkException("Cannot find customer subscription configuration");
        }
        if ("free".equalsIgnoreCase(globalTestConfig.getSubscription().getType().value())) {
            return;
        }
        try {
            subscriptionValidator.checkSubscription(globalTestConfig);
        } catch (Exception e) {
            LogUtil.logException(e);
            throw e;
        }
    }

    private void checkCapabilitiesToConnection(final GlobalTestConfiguration globalTestConfig) {
        Native nativeDevice = globalTestConfig.getNative();
        ConnectionType nativeConnection = nativeDevice.getConnection();
        List<NativeDevice> nativeDevices = nativeDevice.getDevices().getDevice();
        checkCapabilitiesToConnectionFor(nativeConnection, nativeDevices);

        Mobilebrowser mobilebrowser = globalTestConfig.getMobilebrowser();
        ConnectionType mobilebrowserConnection = mobilebrowser.getConnection();
        List<MobilebrowserDevice> mobilebrowserDevices = mobilebrowser.getDevices().getDevice();
        checkCapabilitiesToConnectionFor(mobilebrowserConnection, mobilebrowserDevices);
    }

    private static <T extends AbstractDevice> void checkCapabilitiesToConnectionFor(
            final ConnectionType nativeConnection,
            final List<T> devices) {
        String classNameOfConnection = getClassNameOfConnection(nativeConnection);
        for (AbstractDevice device : devices) {
            String classNameOfCapabilities = getClassNameOfCapability(device);
            checkForCompatibilityOf(classNameOfConnection, classNameOfCapabilities);
        }
    }

    private static String getClassNameOfConnection(final ConnectionType connection) {
        String classNameOfConnection = getClassNameOfActive(connection);
        if (Objects.isNull(classNameOfConnection)) {
            throw new AbsentConnectionException("Device connection is absent");
        }
        return classNameOfConnection;
    }

    private static String getClassNameOfCapability(final AbstractDevice deviceCapabilities) {
        String classNameOfCapabilities = getClassNameOfActive(deviceCapabilities);
        if (Objects.isNull(classNameOfCapabilities)) {
            throw new AbsentCapabilityException("Device capability is absent");
        }
        return classNameOfCapabilities;
    }

    private static void checkForCompatibilityOf(
            final String classNameOfConnection, final String classNameOfCapabilities) {
        List<String> connectionKeyWords = convertCamelCaseToWords(classNameOfConnection);
        List<String> capabilitiesKeyWords = convertCamelCaseToWords(classNameOfCapabilities);
        capabilitiesKeyWords.retainAll(connectionKeyWords);
        if (capabilitiesKeyWords.size() == 0) {
            throw new InvalidCapabilitiesToConnectionException(
                    "Device connection not matched with capabilities");
        }
    }

    @SneakyThrows
    public static String getClassNameOfActive(final Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            setAccessible(field);
            if (field.get(object) != null) {
                return field.get(object).getClass().getSimpleName();
            }
        }
        return null;
    }

    private static void setAccessible(final Field field) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });
    }

    public static List<String> convertCamelCaseToWords(final String camelCase) {
        return Arrays.stream(camelCase.split("(?=[A-Z])"))
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
}
