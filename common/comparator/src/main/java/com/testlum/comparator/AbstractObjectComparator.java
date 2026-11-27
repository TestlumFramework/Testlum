package com.testlum.comparator;

import com.testlum.comparator.exception.MatchException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractObjectComparator<T> {

    protected final Mode mode;

    abstract void compare(T expected, T actual) throws MatchException;
}
