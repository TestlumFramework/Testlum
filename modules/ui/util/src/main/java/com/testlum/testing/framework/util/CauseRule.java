package com.testlum.testing.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

record CauseRule(Pattern pattern, String template) {

    private static final String IGNORE_CASE = "(?i)";
    private static final String DOT_MATCHES_LINE_BREAKS = "(?s)";
    private static final String VERBATIM = "%s";
    private static final CauseRule NONE = new CauseRule(null, null);

    static CauseRule none() {
        return NONE;
    }

    static CauseRule of(final String regex) {
        return of(regex, VERBATIM);
    }

    static CauseRule of(final String regex, final String template) {
        return new CauseRule(Pattern.compile(IGNORE_CASE + DOT_MATCHES_LINE_BREAKS + regex), template);
    }

    Optional<String> apply(final String message) {
        if (Objects.isNull(pattern) || StringUtils.isBlank(message)) {
            return Optional.empty();
        }
        Matcher matcher = pattern.matcher(message);
        return matcher.find()
                ? Optional.of(String.format(template, groupsOf(matcher)))
                : Optional.empty();
    }

    private Object[] groupsOf(final Matcher matcher) {
        if (matcher.groupCount() == 0) {
            return new Object[]{matcher.group().trim()};
        }
        List<Object> groups = new ArrayList<>();
        for (int index = 1; index <= matcher.groupCount(); index++) {
            groups.add(StringUtils.defaultString(matcher.group(index)).trim());
        }
        return groups.toArray();
    }
}
