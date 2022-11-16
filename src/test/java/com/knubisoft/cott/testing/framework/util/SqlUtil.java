package com.knubisoft.cott.testing.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtil {
    private static final int CUT_LIMIT = 100;

    public static String getBrokenQuery(final Exception ex, final String query) {
        final int position = getSqlPositionFromException(ex) - 50;
        return StringUtils.abbreviate(query, position, CUT_LIMIT);
    }

    private static int getSqlPositionFromException(final Exception ex) {
        Pattern p = Pattern.compile("[^0-9]+([0-9]+)$");
        Matcher m = p.matcher(ex.getMessage());
        try {
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception ignored) {
            //ignored
        }
        return 0;
    }
}