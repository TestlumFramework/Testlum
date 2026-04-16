package com.knubisoft.testlum.testing.framework.util;

import java.util.Optional;

public interface DeviceInfoProvider {

    Optional<String> getBrowserInfo(String environment, String alias);

    Optional<String> getMobileBrowserDeviceInfo(String environment, String alias);

    Optional<String> getNativeDeviceInfo(String environment, String alias);
}
