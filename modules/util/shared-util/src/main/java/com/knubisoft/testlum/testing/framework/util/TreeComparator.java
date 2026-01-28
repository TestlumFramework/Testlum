package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.comparator.Comparator;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;

public final class TreeComparator {

    private final Comparator comparator;

    public TreeComparator(final boolean strict) {
        this.comparator = strict ? Comparator.strict() : Comparator.lenient();
    }

    public void compare(final String expected, final String actual) throws ComparisonException {
        try {
            comparator.compare(expected, actual);
        } catch (Throwable t) {
            throw new ComparisonException(t.getMessage());
        }
    }

}
