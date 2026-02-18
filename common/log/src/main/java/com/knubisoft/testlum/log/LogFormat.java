package com.knubisoft.testlum.log;

import org.apache.commons.lang3.StringUtils;

public class LogFormat {

    private static final String TABLE_PATTERN = "%-23s|%-70s";
    private static final String REGEX_NEW_LINE = "[\\r\\n]";
    private static final String NEW_LOG_LINE =
            String.format("%n%19s| ", StringUtils.EMPTY);
    private static final String CONTENT_FORMAT =
            String.format("%n%19s| %-23s|", StringUtils.EMPTY, StringUtils.EMPTY);
    private static final String EXCEPTION_LOG = LogFormat.withRed(
            "----------------    EXCEPTION    -----------------"
            + newLogLine() + "{}" + newLogLine()
            + "--------------------------------------------------");
    private static final String COMMAND_LOG =
            LogFormat.withCyan("------- Command #{} - {} -------");


    private LogFormat() {
        // nop
    }

    public static String table(final String text, final Object arg) {
        String content = String.valueOf(arg);
        return String.format(TABLE_PATTERN, text, content);
    }

    public static String table(final String text) {
        return table(text, "{}");
    }

    public static String with(final Color c, final String text) {
        return c.getCode() + text + Color.RESET.getCode();
    }

    public static String withRed(final String text) {
        return with(Color.RED, text);
    }

    public static String withOrange(final String text) {
        return with(Color.ORANGE, text);
    }

    public static String withGreen(final String text) {
        return with(Color.GREEN, text);
    }

    public static String withCyan(final String text) {
        return with(Color.CYAN, text);
    }

    public static String withYellow(final String text) {
        return with(Color.YELLOW, text);
    }

    public static String newLine() {
        return REGEX_NEW_LINE;
    }

    public static String contentFormat() {
        return CONTENT_FORMAT;
    }

    public static String newLogLine() {
        return NEW_LOG_LINE;
    }

    public static String exceptionLog() {
        return EXCEPTION_LOG;
    }

    public static String commandLog() {
        return COMMAND_LOG;
    }
}
