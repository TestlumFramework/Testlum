package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;

import java.util.List;
import java.util.Map;

public interface ConfigProvider {

    GlobalTestConfiguration provide();

    List<Environment> getEnabledEnvironments();

    Map<String, Integrations> getIntegrations();

    Map<String, UiConfig> getUiConfigs();

    Integrations getDefaultIntegrations();

    UiConfig getDefaultUiConfigs();

    Web getWebSettings(String env);

    Mobilebrowser getMobilebrowserSettings(String env);

    Native getNativeSettings(String env);
}
