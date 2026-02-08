package com.knubisoft.testlum.starter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@UtilityClass
public class Args {

    @Getter
    @RequiredArgsConstructor
    public enum Param {
        CONFIG_FILE(0, "^(-c=|--config=)"),
        PATH_TO_TEST_RESOURCES(1, "^(-p=|--path=)"),
        PATH_TO_SPECIFIC_SCENARIOS(2, "^(-s=|--scenarios=)");

        private final int index;
        private final String regexp;
    }

    public Optional<String> read(final String[] args, final Param param) {
        return safeGet(args, param.index).map(e -> remove(e, param.regexp));
    }

    private String remove(final String input, final String regex) {
        return input.replaceAll(regex, StringUtils.EMPTY);
    }

    private static Optional<String> safeGet(final String[] args, final int index) {
        try {
            return Optional.of(args[index]);
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }
}
