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

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

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
        String nameOfConnection = getNameOfConnection(nativeConnection);
        for (AbstractDevice device : devices) {
            String nameOfCapability = getNameOfCapability(device);
            String connectionNamePrefix = getPrefixOf(nameOfConnection);
            String capabilitiesNamePrefix = getPrefixOf(nameOfCapability);
            checkForCompatibilityOf(connectionNamePrefix, capabilitiesNamePrefix);
        }
    }

    private static String getNameOfConnection(final ConnectionType nativeConnection) {
        String nameOfConnection = getNameOfActive(nativeConnection);
        if (Objects.isNull(nameOfConnection)) {
            throw new AbsentConnectionException("Device connection is absent");
        }
        return nameOfConnection;
    }

    private static String getNameOfCapability(final AbstractDevice device) {
        String nameOfCapability = getNameOfActive(device);
        if (Objects.isNull(nameOfCapability)) {
            throw new AbsentCapabilityException("Device capability is absent");
        }
        return nameOfCapability;
    }

    private static void checkForCompatibilityOf(
            final String connectionNamePrefix, final String capabilitiesNamePrefix) {
        if (!Objects.equals(connectionNamePrefix, capabilitiesNamePrefix)) {
            throw new InvalidCapabilitiesToConnectionException(
                    "Device connection not matched with capabilities");
        }
    }


    public static String getNameOfActive(final Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            String name = getNameOf(object, field);
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    private static String getNameOf(final Object object, final Field field) {
        try {
            Object value = field.get(object);
            if (value != null) {
                return field.getName();
            }
        } catch (IllegalAccessException e) {
            // ignore fields that we cannot access
        }
        return null;
    }

    public static String getPrefixOf(final String str) {
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                break;
            }
            prefix.append(c);
        }
        return prefix.toString();
    }
}
