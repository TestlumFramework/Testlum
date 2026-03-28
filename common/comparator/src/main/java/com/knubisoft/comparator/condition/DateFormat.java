package com.knubisoft.comparator.condition;

import com.knubisoft.comparator.constant.CommonConstant;
import com.knubisoft.comparator.constant.RegexpConstant;
import com.knubisoft.comparator.exception.MatchException;
import com.knubisoft.comparator.util.ConditionalTypeComparator;
import com.knubisoft.comparator.util.LogMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.regex.Pattern;


@Getter
@RequiredArgsConstructor
public enum DateFormat {
    DATE_TIME_FORMAT_WITH_DASH(String.format(CommonConstant.DATE_TIME_TEMPLATE,
            CommonConstant.DASH, CommonConstant.DASH),
            RegexpConstant.DATE_TIME_WITH_DASH_PATTERN,
            TemporalKind.DATE_TIME),
    DATE_TIME_FORMAT_WITH_SLASH(String.format(CommonConstant.DATE_TIME_TEMPLATE,
            CommonConstant.SLASH, CommonConstant.SLASH),
            RegexpConstant.DATE_TIME_WITH_SLASH_PATTERN,
            TemporalKind.DATE_TIME),
    DATE_FORMAT_WITH_DASH(String.format(CommonConstant.DATE_TEMPLATE, CommonConstant.DASH, CommonConstant.DASH),
            RegexpConstant.DATE_WITH_DASH_PATTERN,
            TemporalKind.DATE),
    DATE_FORMAT_WITH_SLASH(String.format(CommonConstant.DATE_TEMPLATE, CommonConstant.SLASH, CommonConstant.SLASH),
            RegexpConstant.DATE_WITH_SLASH_PATTERN,
            TemporalKind.DATE),
    TIME_FORMAT(CommonConstant.TIME_FORMAT, Pattern.compile(RegexpConstant.TIME), TemporalKind.TIME);

    private final String format;
    private final Pattern pattern;
    private final TemporalKind kind;

    private enum TemporalKind {
        DATE_TIME, DATE, TIME
    }

    public static DateFormat findFormat(final String value) {
        return Arrays.stream(DateFormat.values())
                .filter(fmt -> fmt.getPattern().matcher(value).matches())
                .findFirst()
                .orElseThrow(() -> new MatchException(LogMessage.FORMAT_NOT_SUPPORTED));
    }

    public Comparable<?> parse(final String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.format);
        try {
            return switch (kind) {
                case DATE_TIME -> LocalDateTime.parse(value, formatter);
                case DATE -> LocalDate.parse(value, formatter);
                case TIME -> LocalTime.parse(value, formatter);
            };
        } catch (DateTimeParseException e) {
            throw new MatchException(e.getMessage());
        }
    }

    public Comparable<?> now() {
        return switch (kind) {
            case DATE_TIME -> LocalDateTime.now();
            case DATE -> LocalDate.now();
            case TIME -> LocalTime.now();
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> boolean compareWith(final String actual,
                                                         final String expected,
                                                         final Operator operator) {
        return ConditionalTypeComparator.compareConditions((T) parse(actual), (T) parse(expected), operator);
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> boolean compareWithNow(final String actual, final Operator operator) {
        return ConditionalTypeComparator.compareConditions((T) parse(actual), (T) now(), operator);
    }
}
