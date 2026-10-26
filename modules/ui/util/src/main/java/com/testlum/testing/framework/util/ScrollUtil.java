package com.testlum.testing.framework.util;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScrollUtil {

    public static int parseScrollValue(final String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new DefaultFrameworkException(ExceptionMessage.INVALID_SCROLL_VALUE, value);
        }
    }
}
