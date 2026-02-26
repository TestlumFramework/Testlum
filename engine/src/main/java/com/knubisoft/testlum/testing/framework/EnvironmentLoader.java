package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EnvironmentLoader {

    @Qualifier("uiConfig")
    private final Map<String, UiConfig> uiConfigMap;

    public Optional<Web> getCurrentEnvWebSettings() {
        return getWebSettings(EnvManager.currentEnv());
    }

    public Optional<Mobilebrowser> getCurrentEnvMobileBrowserSettings() {
        return getMobileBrowserSettings(EnvManager.currentEnv());
    }

    public Optional<Native> getCurrentEnvNativeSettings() {
        return getNativeSettings(EnvManager.currentEnv());
    }

    public Optional<Web> getWebSettings(final String environment) {
        return Optional.ofNullable(uiConfigMap.get(environment)).map(UiConfig::getWeb);
    }

    public Optional<Mobilebrowser> getMobileBrowserSettings(final String environment) {
        return Optional.ofNullable(uiConfigMap.get(environment)).map(UiConfig::getMobilebrowser);
    }

    public Optional<Native> getNativeSettings(final String environment) {
        return Optional.ofNullable(uiConfigMap.get(environment)).map(UiConfig::getNative);
    }

}
