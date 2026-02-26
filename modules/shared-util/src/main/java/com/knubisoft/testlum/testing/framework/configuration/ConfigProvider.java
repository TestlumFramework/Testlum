package com.knubisoft.testlum.testing.framework.configuration;

import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.Web;

public interface ConfigProvider {

    Web getWebSettings(String env);

    Mobilebrowser getMobileBrowserSettings(String env);

    Native getNativeSettings(String env);
}
