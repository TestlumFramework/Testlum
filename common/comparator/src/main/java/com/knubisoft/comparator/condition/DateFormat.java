package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.constant.CommonConstant;
import com.knubisoft.comparator.constant.RegexpConstant;
import com.knubisoft.comparator.exception.MatchException;
import com.knubisoft.comparator.util.LogMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.regex.Pattern;


@Getter
@RequiredArgsConstructor
public enum DateFormat {
    DATE_TIME_FORMAT_WITH_DASH(String.format(CommonConstant.DATE_TIME_TEMPLATE,
            CommonConstant.DASH, CommonConstant.DASH),
            RegexpConstant.DATE_TIME_WITH_DASH_PATTERN),
    DATE_TIME_FORMAT_WITH_SLASH(String.format(CommonConstant.DATE_TIME_TEMPLATE,
            CommonConstant.SLASH, CommonConstant.SLASH),
            RegexpConstant.DATE_TIME_WITH_SLASH_PATTERN),
    DATE_FORMAT_WITH_DASH(String.format(CommonConstant.DATE_TEMPLATE, CommonConstant.DASH, CommonConstant.DASH),
            RegexpConstant.DATE_WITH_DASH_PATTERN),
    DATE_FORMAT_WITH_SLASH(String.format(CommonConstant.DATE_TEMPLATE, CommonConstant.SLASH, CommonConstant.SLASH),
            RegexpConstant.DATE_WITH_SLASH_PATTERN),
    TIME_FORMAT(CommonConstant.TIME_FORMAT, Pattern.compile(RegexpConstant.TIME));

    private final String format;
    private final Pattern pattern;

    public static SimpleDateFormat getFormatter(final String value) {
        DateFormat dateFormat = Arrays.stream(DateFormat.values())
                .filter(format -> format.getPattern().matcher(value).matches())
                .findFirst()
                .orElseThrow(() -> new MatchException(LogMessage.FORMAT_NOT_SUPPORTED));
        return new SimpleDateFormat(dateFormat.getFormat());
    }
}
