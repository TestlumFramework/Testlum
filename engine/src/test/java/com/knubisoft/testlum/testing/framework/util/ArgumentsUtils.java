package com.knubisoft.testlum.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@UtilityClass
public class ArgumentsUtils {

    public static final String CONFIG_FILE_ARGUMENT_START_WITH = "^(-c=|--config=)";
    public static final String PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH = "^(-p=|--path=)";
    public static final String PATH_TO_SPECIFIC_SCENARIOS_ARGUMENT_START_WITH = "^(-s=|--scenarios=)";

    public String getConfigurationFileName(final String configurationFileNameArgument) {
        return configurationFileNameArgument.replaceAll(CONFIG_FILE_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public String getPathToTestResources(final String pathArgument) {
        return pathArgument.replaceAll(PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public Optional<String> getScenarioScope(final String[] args) {
        if (args.length == 3) {
            return Optional.of(args[2]
                    .replaceAll(PATH_TO_SPECIFIC_SCENARIOS_ARGUMENT_START_WITH, StringUtils.EMPTY));
        }
        return Optional.empty();
    }
}
