package com.testlum.testing.framework.util;

import java.util.Objects;
import java.util.regex.Pattern;

public class WebhookUrlUtil {

    private static final Pattern SCHEME_AND_HOST = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/?#]*");


    /** Extracts and normalizes the path of a url.
     * <p>
     * Example: {@code https://x.ngrok.app/hooks/pay/?a=1 -> /hooks/pay}.
     */
    public static String pathOf(final String url) {
        String path = SCHEME_AND_HOST.matcher(Objects.requireNonNull(url, "url").trim()).replaceFirst("");
        int queryOrFragment = indexOfQueryOrFragment(path);
        if (queryOrFragment >= 0) {
            path = path.substring(0, queryOrFragment);
        }
        return normalizePath(path);
    }

    /**
     * Leading slash, no trailing slash (except the root path).
     * */
    public static String normalizePath(final String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }


    private static int indexOfQueryOrFragment(final String path) {
        int query = path.indexOf('?');
        int fragment = path.indexOf('#');
        if (query < 0) {
            return fragment;
        }
        return fragment < 0 ? query : Math.min(query, fragment);
    }
}
