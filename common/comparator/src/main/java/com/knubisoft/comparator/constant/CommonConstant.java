package com.knubisoft.comparator.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstant {

    public static final String T_LEFT_BRACKET = "t(";
    public static final String RIGHT_BRACKET = ")";

    public static final int EXTRACT_ACTION_THREE = 3;

    public static final String EMPTY = "";
    public static final String DASH = "-";
    public static final String SLASH = "/";

    public static final String DATE_TIME_TEMPLATE = "yyyy%sMM%sdd HH:mm:ss";
    public static final String DATE_TEMPLATE = "yyyy%sMM%sdd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String ALL_EXPRESSION_TO_REGEXP = "^%s$";
}
