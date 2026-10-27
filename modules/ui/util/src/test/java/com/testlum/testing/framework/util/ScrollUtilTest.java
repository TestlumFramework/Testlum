package com.testlum.testing.framework.util;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrollUtilTest {

    @Test
    void parsesPositiveInteger() {
        assertEquals(150, ScrollUtil.parseScrollValue("150"));
    }

    @Test
    void parsesNegativeInteger() {
        assertEquals(-100, ScrollUtil.parseScrollValue("-100"));
    }

    @Test
    void parsesZero() {
        assertEquals(0, ScrollUtil.parseScrollValue("0"));
    }

    @Test
    void throwsDefaultFrameworkExceptionForNonInteger() {
        DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> ScrollUtil.parseScrollValue("not_a_number"));
        assertEquals(String.format(ExceptionMessage.INVALID_SCROLL_VALUE, "not_a_number"), ex.getMessage());
    }

    @Test
    void throwsDefaultFrameworkExceptionForNull() {
        DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                () -> ScrollUtil.parseScrollValue(null));
        assertEquals(String.format(ExceptionMessage.INVALID_SCROLL_VALUE, "null"), ex.getMessage());
    }
}
