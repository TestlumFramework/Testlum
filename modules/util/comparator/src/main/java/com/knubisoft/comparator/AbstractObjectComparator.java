package com.knubisoft.comparator;

import com.knubisoft.comparator.exception.MatchException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractObjectComparator<T> {

    protected final Mode mode;

    abstract void compare(T expected, T actual) throws MatchException;
}
