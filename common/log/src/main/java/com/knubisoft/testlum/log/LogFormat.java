package com.knubisoft.testlum.log;

public class LogFormat {

    private static final String TABLE_PATTERN = "%-23s|%-70s";
    private static final String REGEX_NEW_LINE = "[\\r\\n]";

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

}
