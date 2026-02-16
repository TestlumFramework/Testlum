package com.knubisoft.comparator.condition;

public interface ConditionComparator {

    boolean compare(String actual, String expected, Operator operator);
}
