package com.knubisoft.testlum.testing.framework.util;


import com.knubisoft.comparator.Comparator;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class TreeComparator {

    private static final Comparator COMPARATOR = Comparator.strict();

    public void compare(final String expected, final String actual) throws ComparisonException {
        try {
            COMPARATOR.compare(expected, actual);
        } catch (Throwable t) {
            throw new ComparisonException(t.getMessage());
        }
    }

}
