package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StringPrettifier {
    public String prettify(final String string) {
        return string.replaceAll("\\s+", DelimiterConstant.EMPTY);
    }
}
