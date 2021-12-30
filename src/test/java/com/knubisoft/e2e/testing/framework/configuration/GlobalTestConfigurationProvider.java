package com.knubisoft.e2e.testing.framework.configuration;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.parser.XMLParser;
import com.knubisoft.e2e.testing.model.global_config.GlobalTestConfiguration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.DASH;
import static com.knubisoft.e2e.testing.framework.constant.SeleniumConstant.PROFILE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CANNOT_FIND_PROFILE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalTestConfigurationProvider {

    private static final GlobalTestConfiguration GLOBAL_TEST_CONFIGURATION = init();

    public static GlobalTestConfiguration provide() {
        return GLOBAL_TEST_CONFIGURATION;
    }

    private static GlobalTestConfiguration init() {
        URL url = Thread.currentThread().getContextClassLoader().getResource(getConfigNameByProfile());
        File xmlFile = new File(Objects.requireNonNull(url).getFile());
        return XMLParser.forGlobalTestConfiguration().process(xmlFile);
    }

    private static String getConfigNameByProfile() {
        if (System.getProperty(PROFILE) != null) {
            return TestResourceSettings.GLOBAL_CONFIG_FILENAME + DASH + System.getProperty(PROFILE) + TestResourceSettings.XML_SUFFIX;
        } else {
            throw new DefaultFrameworkException(CANNOT_FIND_PROFILE);
        }
    }
}
