package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.Web;

import java.util.Map;

public interface ConfigProvider {

    GlobalTestConfiguration provide();

    Map<String, Integrations> getIntegrations();

    Integrations getDefaultIntegrations();

    Web getWebSettings(String env);

    Mobilebrowser getMobilebrowserSettings(String env);

    Native getNativeSettings(String env);
}
