package com.testlum.testing.framework.configuration;

import com.testlum.testing.framework.EnvironmentLoader;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.Mobilebrowser;
import com.testlum.testing.model.global_config.Native;
import com.testlum.testing.model.global_config.Web;
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

    private Supplier<? extends RuntimeException> noConfigurationFound(final String env) {
        return () -> new DefaultFrameworkException("No configuration found for " + env);
    }

}
