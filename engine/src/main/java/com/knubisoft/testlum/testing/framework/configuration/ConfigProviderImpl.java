package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.Web;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ConfigProviderImpl implements ConfigProvider {

    private final GlobalTestConfigurationProvider config;

    public ConfigProviderImpl() {
        this.config = GlobalTestConfigurationProvider.get();
    }

    @Override
    public GlobalTestConfiguration provide() {
        return config.provide();
    }

    @Override
    public Map<String, Integrations> getIntegrations() {
        return config.getIntegrations();
    }

    @Override
    public Integrations getDefaultIntegrations() {
        return config.getDefaultIntegrations();
    }

    @Override
    public Web getWebSettings(final String env) {
        return config.getWebSettings(env);
    }

    @Override
    public Mobilebrowser getMobilebrowserSettings(final String env) {
        return config.getMobileBrowserSettings(env);
    }

    @Override
    public Native getNativeSettings(final String env) {
        return config.getNativeSettings(env);
    }


}
