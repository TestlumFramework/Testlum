package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class NavigateUtil {

    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");

    public void navigateTo(final String path, final ExecutorDependencies dependencies) {
        String url = getUrl(path, dependencies);
        dependencies.getDriver().navigate().to(url);
    }

    private String getUrl(final String path, final ExecutorDependencies dependencies) {
        if (HTTP_PATTERN.matcher(path).matches()) {
            return path;
        }
        if (UiType.MOBILE_BROWSER == dependencies.getUiType()) {
            return GlobalTestConfigurationProvider.getMobilebrowserSettings(dependencies.getEnvironment())
                    .getBaseUrl() + path;
        }
        return GlobalTestConfigurationProvider.getWebSettings(dependencies.getEnvironment()).getBaseUrl() + path;
    }
}
