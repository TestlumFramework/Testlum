package com.knubisoft.cott.testing.framework.exception;

import static java.lang.String.format;

public class IncorrectQueryForVarException extends RuntimeException {
    private static final String INCORRECT_RESULT_OF_QUERY = "Incorrect result of query: \"%s\". "
            + "It has %d rows of values, but must have only one.";

    public IncorrectQueryForVarException(final String query, final int numberOfRows) {
        super(format(INCORRECT_RESULT_OF_QUERY, query, numberOfRows));
    }
}
