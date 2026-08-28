package com.testlum.testing.framework.xml;

import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record XsdIssue(XsdSeverity severity,
                       int line,
                       int column,
                       String code,
                       List<String> args,
                       String rawMessage) {

    private static final Pattern CODED_MESSAGE = Pattern.compile("^(cvc-[\\w.\\-]+):\\s*(.*)$", Pattern.DOTALL);

    private static final Pattern QUOTED_ARGUMENT = Pattern.compile("'([^']*)'");

    private static final String UNKNOWN_CODE = "";

    public XsdIssue {
        args = args == null ? List.of() : List.copyOf(args);
    }

    public static XsdIssue of(final XsdSeverity severity, final SAXParseException cause) {
        String raw = cause.getMessage() == null ? UNKNOWN_CODE : cause.getMessage();
        int line = cause.getLineNumber();
        int column = cause.getColumnNumber();
        Matcher matcher = CODED_MESSAGE.matcher(raw);
        if (!matcher.matches()) {
            return new XsdIssue(severity, line, column, UNKNOWN_CODE, List.of(), raw);
        }
        return new XsdIssue(severity, line, column, matcher.group(1), argumentsOf(matcher.group(2)), raw);
    }

    public String arg(final int index) {
        return index >= 0 && index < args.size() ? args.get(index) : null;
    }

    public String messageWithoutCode() {
        Matcher matcher = CODED_MESSAGE.matcher(rawMessage);
        return matcher.matches() ? matcher.group(2) : rawMessage;
    }

    public boolean blocksValidation() {
        return severity != XsdSeverity.WARNING;
    }

    private static List<String> argumentsOf(final String body) {
        List<String> found = new ArrayList<>();
        Matcher matcher = QUOTED_ARGUMENT.matcher(body);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }
}
