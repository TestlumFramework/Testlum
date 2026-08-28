package com.testlum.comparator.condition;

public interface ConditionComparator {

    boolean compare(String actual, String expected, Operator operator);
}
