package com.knubisoft.comparator.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogMessage {

    public static final String CONTENT_DOES_MATCH = "Content doesn't match";
    public static final String WRONG_OPERATION = "Expression [%s] is not conditional operation";
    public static final String TYPE_NOT_SUPPORTED = "Type [%s] not supported";
    public static final String PROPERTY_NOT_EQUAL = "Property [%s] is not equal to [%s]";
    public static final String PROPERTY_NOT_MATCH = "Property [%s] should match pattern [%s]";
    public static final String CONDITIONALS_DO_NOT_MATCH = "Conditionals actual-[%s] expected-[%s] do not match";
    public static final String FORMAT_NOT_SUPPORTED = "Date format not supported";
    public static final String WRONG_OPERATOR = "Wrong operator in expression %s you should use only [ >, >=, <, <= ]";
}
