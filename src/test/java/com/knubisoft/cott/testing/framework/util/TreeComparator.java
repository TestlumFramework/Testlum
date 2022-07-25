package com.knubisoft.cott.testing.framework.util;


import com.knubisoft.cott.testing.framework.exception.ComparisonException;
import lombok.experimental.UtilityClass;
import org.imagination.comparator.Comparator;

import java.util.Collections;

@UtilityClass
public final class TreeComparator {

    private static final Comparator COMPARATOR = Comparator.java().strict(Collections.emptyMap());

    public void compare(final String expected, final String actual) throws ComparisonException {
        try {
            COMPARATOR.compare(expected, actual);
        } catch (Throwable t) {
            throw new ComparisonException(t.getMessage());
        }
    }

}
