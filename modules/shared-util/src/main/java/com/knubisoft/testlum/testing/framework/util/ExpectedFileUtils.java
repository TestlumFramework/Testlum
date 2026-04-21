package com.knubisoft.testlum.testing.framework.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class servers as a  filter to check if expected file comes from variations csv table.
 * <p>Usually position from {@link com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies} is used to create expected file, but there we get position from expected file by regex and apply to newly created actual file name
 *
 *
 */
@UtilityClass
public class ExpectedFileUtils {

    private static final String FILENAME_FORMAT_TO_SAVE = "action_%s_actual.json";
    private static final String EXPECTED_FROM_VARIATIONS_STEP_FILE_REGEX = "(expected_\\d+_\\d+.json)";
    private static final Pattern EXPECTED_STEP_NUMBER_PATTERN = Pattern.compile("(\\d+_\\d+)");

    public String resolveActualNameBasedOnExpectedFileName(final String expectedFileName,
                                                           final int position) {
        if (expectedFileName.matches(EXPECTED_FROM_VARIATIONS_STEP_FILE_REGEX)) {
            Matcher matcher = EXPECTED_STEP_NUMBER_PATTERN.matcher(expectedFileName);
            if (matcher.find()) {
                String variationStepNumber = matcher.group(1);
                return String.format(FILENAME_FORMAT_TO_SAVE, variationStepNumber);
            }
        }
        return String.format(FILENAME_FORMAT_TO_SAVE, position);
    }
}
