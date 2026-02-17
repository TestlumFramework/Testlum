package com.knubisoft.comparator;


import com.knubisoft.comparator.exception.MatchException;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.comparator.util.LogMessage.CONTENT_DOES_MATCH;

public class StringLinesComparator extends AbstractObjectComparator<String> {

    public StringLinesComparator(final Mode mode) {
        super(mode);
    }

    @Override
    public void compare(final String expected, final String actual) throws MatchException {
        List<String> expectedLines = getLines(expected);
        List<String> actualLines = getLines(actual);

        if (expectedLines.size() != actualLines.size()) {
            throw new MatchException(CONTENT_DOES_MATCH);
        }

        for (int i = 0, size = expectedLines.size(); i < size; i++) {
            compareLine(expectedLines.get(i), actualLines.get(i));
        }
    }

    private List<String> getLines(final String input) {
        return new BufferedReader(new StringReader(input))
                .lines()
                .toList();
    }

    private void compareLine(final String expected, final String actual) throws MatchException {
        if (!Objects.equals(expected, actual)) {
            new StringComparator(mode).compare(expected, actual);
        }
    }
}
