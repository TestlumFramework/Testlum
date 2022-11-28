package com.knubisoft.cott.testing.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class SqlUtil {
    private static final int CUT_LIMIT = 100;
    private static final int OFFSET_LIMIT = 2;
    private static final Pattern BAD_SQL_POSITION_PATTERN = Pattern.compile("([0-9]+)");

    public String getBrokenQuery(final Exception ex, final String query) {
        final int position = getSqlPositionFromException(ex) - OFFSET_LIMIT;
        return StringUtils.abbreviate(query, position, CUT_LIMIT);
    }

    private int getSqlPositionFromException(final Exception ex) {
        Matcher m = BAD_SQL_POSITION_PATTERN.matcher(ex.getMessage());
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
