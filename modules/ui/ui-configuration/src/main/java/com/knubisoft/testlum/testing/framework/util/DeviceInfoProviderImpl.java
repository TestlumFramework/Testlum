package com.knubisoft.testlum.testing.framework.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceInfoProviderImpl implements DeviceInfoProvider {

    private final BrowserUtil browserUtil;
    private final MobileUtil mobileUtil;

    @Override
    public Optional<String> getBrowserInfo(final String environment, final String alias) {
        return browserUtil.getBrowserBy(environment, alias)
                .map(browserUtil::getBrowserInfo);
    }

    @Override
    public Optional<String> getMobileBrowserDeviceInfo(final String environment, final String alias) {
        return mobileUtil.getMobileBrowserDeviceBy(environment, alias)
                .map(mobileUtil::getMobileBrowserDeviceInfo);
    }

    @Override
    public Optional<String> getNativeDeviceInfo(final String environment, final String alias) {
        return mobileUtil.getNativeDeviceBy(environment, alias)
                .map(mobileUtil::getNativeDeviceInfo);
    }
}
