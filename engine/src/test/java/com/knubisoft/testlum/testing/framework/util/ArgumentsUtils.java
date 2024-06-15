package com.knubisoft.testlum.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class ArgumentsUtils {

    public static final String CONFIG_FILE_ARGUMENT_START_WITH = "^(-c=|--config=)";
    public static final String PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH = "^(-p=|--path=)";
    public static final String USERNAME_ARGUMENT_START_WITH = "^(-u=|--username=)";

    public String getConfigurationFileName(final String configurationFileNameArgument) {
        return configurationFileNameArgument.replaceAll(CONFIG_FILE_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public String getPathToTestResources(final String pathArgument) {
        return pathArgument.replaceAll(PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public String getUsername(final String usernameArgument) {
        return usernameArgument.replaceAll(USERNAME_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }
}
