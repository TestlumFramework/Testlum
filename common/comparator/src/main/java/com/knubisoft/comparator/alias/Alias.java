package com.knubisoft.comparator.alias;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.knubisoft.comparator.constant.RegexpConstant.DATE_TIME_WITH_DASH;
import static com.knubisoft.comparator.constant.RegexpConstant.DOMAIN_NAME;
import static com.knubisoft.comparator.constant.RegexpConstant.GOOD_IRI_CHAR;

@Getter
@RequiredArgsConstructor
public enum Alias {
    ANY("any", Pattern.compile(".*", Pattern.DOTALL)),
    DIGIT("digit", Pattern.compile("-?\\d+(\\.\\d+)?")),
    MONEY("money", Pattern.compile("(0|[-+]?[1-9]\\d*([,.]\\d+)?)")),
    EMAIL("email", Pattern.compile("([a-zA-Z0-9+._%\\-]{1,256}"
            + "@"
            + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}"
            + "("
            + "\\."
            + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}"
            + "))")),
    IP("ip", Pattern.compile("((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
            + "|[1-9][0-9]|[0-9]))")),
    URL("url", Pattern.compile("((?:(http|https|Http|Https|rtsp|Rtsp):"
            + "//(?:(?:[a-zA-Z0-9$\\-_.+!*'()"
            + ",;?&=]|(?:%[a-fA-F0-9]{2})){1,64}(?::(?:[a-zA-Z0-9$\\-_"
            + ".+!*'(),;?&=]|%[a-fA-F0-9]{2}){1,25})?@)?)?"
            + "(?:" + DOMAIN_NAME + ")"
            + "(?::\\d{1,5})?)"
            + "(/(?:(?:[" + GOOD_IRI_CHAR + ";/?:@&=#~"
            + "\\-.+!*'(),_])|(?:%[a-fA-F0-9]{2}))*)?"
            + "(?:\\b|$)")),
    UUID("uuid",
            Pattern.compile("\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}")),
    COLOR("color", Pattern.compile("#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})")),
    Y_M_D_DASH("y-m-d", Pattern.compile("(\\d{4}-(?:0[1-9]|1[012])-(?:0[1-9]|[12][0-9]|3[01]))")),
    Y_M_D_SLASH("y/m/d", Pattern.compile("(\\d{4}/(?:0[1-9]|1[012])/(?:0[1-9]|[12][0-9]|3[01]))")),
    D_M_Y_DASH("d-m-y", Pattern.compile("((?:0[1-9]|[12][0-9]|3[01])-(?:0[1-9]|1[012])-\\d{4})")),
    D_M_Y_SLASH("d/m/y", Pattern.compile("((?:0[1-9]|[12][0-9]|3[01])/(?:0[1-9]|1[012])/\\d{4})")),
    NOT_EMPTY("notEmpty", Pattern.compile("(?=\\s*\\S).*", Pattern.DOTALL)),
    DATE_TIME("dateTime", Pattern.compile(DATE_TIME_WITH_DASH));

    private final String aliasName;
    private final Pattern pattern;

    public static Pattern getPattern(final String aliasName) {
        return Arrays.stream(Alias.values())
                .filter(a -> a.getAliasName().equals(aliasName))
                .findFirst()
                .map(Alias::getPattern)
                .orElse(Pattern.compile(aliasName));
    }

}
