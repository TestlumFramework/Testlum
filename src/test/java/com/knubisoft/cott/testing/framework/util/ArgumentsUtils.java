package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.exception.InvalidArgumentException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INVALID_ARGUMENTS_INPUT;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INVALID_CONFIG_FILE_NAME_ARGUMENT;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INVALID_PATH_TO_INITIAL_STRUCTURE_GENERATION_ARGUMENT;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.INVALID_PATH_TO_RESOURCES_ARGUMENT;
import static java.lang.String.format;

@UtilityClass
public class ArgumentsUtils {

    public static final String CONFIG_FILE_ARGUMENT_START_WITH = "^(-c=|--config=)";
    public static final String PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH = "^(-p=|--path=)";
    public static final String PATH_TO_INITIAL_STRUCTURE_GENERATION_ARGUMENT_START_WITH = "^(-g=|--generate=)";
    public static final Pattern CONFIG_FILE_PATTERN = Pattern.compile("^(-c=|--config=)[a-zA-Z0-9.\\-_:*#]+(.xml)$");
    public static final Pattern LINUX_PATH_PATTERN =
            Pattern.compile("^(-p=|--path=)(./|/|(./[a-zA-Z0-9_-]+)|(/[a-zA-Z0-9_-]+))+$");
    public static final Pattern WIN_PATH_PATTERN =
            Pattern.compile("^(-p=|--path=)(.\\\\|\\\\|(.\\\\[a-zA-Z0-9_-]+)|(\\\\[a-zA-Z0-9_-]+))+$");
    public static final Pattern STRUCTURE_PATH_PATTERN =
            Pattern.compile("^(-g=|--generate=)(./|/|(./[a-zA-Z0-9_-]+)|(/[a-zA-Z0-9_-]+))+$");

    public void validateInputArguments(final String[] args) {
        if (args.length == 2) {
            validateConfigFileNameFromArgs(args[0]);
            validatePathFromArgs(args[1]);
        } else if (args.length == 1) {
            validateInitialStructurePathFromArgs(args[0]);
        } else {
            throw new InvalidArgumentException(INVALID_ARGUMENTS_INPUT);
        }
    }

    public void validateConfigFileNameFromArgs(final String configFileName) {
        if (!CONFIG_FILE_PATTERN.matcher(configFileName).matches()) {
            throw new InvalidArgumentException(format(INVALID_CONFIG_FILE_NAME_ARGUMENT, configFileName));
        }
    }

    public void validatePathFromArgs(final String path) {
        if (!(LINUX_PATH_PATTERN.matcher(path).matches() || WIN_PATH_PATTERN.matcher(path).matches())) {
            throw new InvalidArgumentException(format(INVALID_PATH_TO_RESOURCES_ARGUMENT, path));
        }
    }

    public void validateInitialStructurePathFromArgs(final String path) {
        if (!STRUCTURE_PATH_PATTERN.matcher(path).matches()) {
            throw new InvalidArgumentException(format(INVALID_PATH_TO_INITIAL_STRUCTURE_GENERATION_ARGUMENT, path));
        }
    }

    public String getConfigurationFileName(final String configurationFileNameArgument) {
        return configurationFileNameArgument.replaceAll(CONFIG_FILE_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public String getPathToTestResources(final String pathArgument) {
        return pathArgument.replaceAll(PATH_TO_TEST_RESOURCES_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }

    public String getPathToInitialStructureGeneration(final String pathArgument) {
        return pathArgument.replaceAll(PATH_TO_INITIAL_STRUCTURE_GENERATION_ARGUMENT_START_WITH, StringUtils.EMPTY);
    }
}
