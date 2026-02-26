package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class ConfigProviderImpl implements ConfigProvider {

    private final EnvironmentLoader loader;

    @Override
    public Web getWebSettings(final String env) {
        return loader.getWebSettings(env).orElseThrow(noConfigurationFound(env));
    }

    @Override
    public Mobilebrowser getMobileBrowserSettings(final String env) {
        return loader.getMobileBrowserSettings(env).orElseThrow(noConfigurationFound(env));
    }

    @Override
    public Native getNativeSettings(final String env) {
        return loader.getNativeSettings(env).orElseThrow(noConfigurationFound(env));
    }

    private Supplier<? extends RuntimeException> noConfigurationFound(String env) {
        return ()-> new DefaultFrameworkException("No configuration found for " + env);
    }

}
