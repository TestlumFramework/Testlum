package com.testlum.testing.framework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrefixUtil {

    public static String applyPrefix(final String prefix, final String fileName) {
        if (StringUtils.isNotBlank(prefix)) {
            return prefix + "_" + fileName;
        }
        return fileName;
    }
}
