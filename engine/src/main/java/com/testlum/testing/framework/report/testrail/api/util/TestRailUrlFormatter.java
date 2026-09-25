package com.testlum.testing.framework.report.testrail.api.util;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public final class TestRailUrlFormatter {

    private static final String API_PATH = "index.php?/api/v2/";
    private static final String INDEX_PHP = "index.php";
    private static final String API_SEGMENT = "/api/v2";
    private static final String HTTPS_SCHEME = "https://";
    private static final String SCHEME_SEPARATOR = "://";
    private static final String SLASH = "/";

    private TestRailUrlFormatter() {
    }

    public static String format(final String url) {
        String instanceUrl = StringUtils.isBlank(url) ? StringUtils.EMPTY : cutTrailingSlashes(url.trim());
        if (StringUtils.isBlank(instanceUrl)) {
            throw new DefaultFrameworkException(TestRailConstants.URL_NOT_CONFIGURED);
        }
        String formattedUrl = formatApiPath(withScheme(instanceUrl));
        if (!formattedUrl.equals(url)) {
            log.info(TestRailConstants.LOG_URL_FORMATTED, url, formattedUrl);
        }
        return formattedUrl;
    }

    private static String cutTrailingSlashes(final String url) {
        String stripped = url;
        while (stripped.endsWith(SLASH)) {
            stripped = stripped.substring(0, stripped.length() - SLASH.length());
        }
        return stripped;
    }

    private static String withScheme(final String url) {
        String urlWithScheme = url.contains(SCHEME_SEPARATOR) ? url : HTTPS_SCHEME + url;
        return urlWithScheme.replaceAll("(?i)(https?://)/+", "$1");
    }

    private static String formatApiPath(final String url) {
        int indexPhpPosition = StringUtils.indexOfIgnoreCase(url, INDEX_PHP);
        if (indexPhpPosition < 0) {
            return url + SLASH + API_PATH;
        }
        if (StringUtils.containsIgnoreCase(url.substring(indexPhpPosition), API_SEGMENT)) {
            return url + SLASH;
        }
        return url.substring(0, indexPhpPosition) + API_PATH;
    }
}
