package com.knubisoft.testlum.starter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Args {

    @RequiredArgsConstructor
    @Getter
    public enum Param {
        CONFIG_FILE("^(?:-c|--config)=(.*)$"),
        PATH_TO_TEST_RESOURCES("^(?:-p|--path)=(.*)$"),
        PATH_TO_SPECIFIC_SCENARIOS("^(?:-s|--scenarios)=(.*)$");

        private final String regexp;
    }

    public Optional<String> read(final String[] args, final Param param) {
        return tryFindByRegexp(args, param.regexp);
    }

    private static Optional<String> tryFindByRegexp(final String[] args, final String regexp) {
        Pattern p = Pattern.compile(regexp);
        for (final String arg : args) {
            Matcher m = p.matcher(arg);
            if (m.matches()) {
                return Optional.of(m.group(1));
            }
        }
        return Optional.empty();
    }
}
