package com.knubisoft.testlum.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@UtilityClass
public class ArgumentsUtils {

    public static final String CONFIG_FILE_ARGUMENT_START_WITH = "^(-c=|--config=)";
    public static final String PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH = "^(-p=|--path=)";
    public static final String PATH_TO_SPECIFIC_SCENARIOS_ARGUMENT_START_WITH = "^(-s=|--scenarios=)";

    public Optional<String> getConfigurationFileName(final Optional<String> configurationFileNameArgument) {
        return configurationFileNameArgument.map(e ->
                e.replaceAll(CONFIG_FILE_ARGUMENT_START_WITH, StringUtils.EMPTY));
    }

    public Optional<String> getPathToTestResources(final Optional<String> pathArgument) {
        return pathArgument.map(e ->
                e.replaceAll(PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH, StringUtils.EMPTY));
    }

    public Optional<String> getScenarioScope(final Optional<String> pathToSpecificScenario) {
        return pathToSpecificScenario.map(e ->
                e.replaceAll(PATH_TO_SPECIFIC_SCENARIOS_ARGUMENT_START_WITH, StringUtils.EMPTY));
    }
}
