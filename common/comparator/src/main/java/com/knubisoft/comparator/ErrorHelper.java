package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorHelper {

    public static void raise(final String msg) throws MatchException {
        throw new MatchException(msg);
    }

    public static void raise(final boolean flag, final String msg) throws MatchException {
        if (flag) {
            raise(msg);
        }
    }
}
