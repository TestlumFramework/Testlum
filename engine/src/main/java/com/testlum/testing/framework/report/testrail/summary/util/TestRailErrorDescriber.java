package com.testlum.testing.framework.report.testrail.summary.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * Turns an API failure into one short line fit for a table cell:
 */
@Component
public class TestRailErrorDescriber {

    private static final int MAX_REASON_LENGTH = 180;
    private static final String HTTP_REASON_TEMPLATE = "HTTP %s: %s";
    private static final String COMPACT_ELLIPSIS = "...";
    private static final String WHITESPACE_REGEX = "\\s+";

    public String describe(final Exception exception) {
        if (exception instanceof RestClientResponseException responseException) {
            return compactCause(String.format(HTTP_REASON_TEMPLATE, responseException.getStatusCode().value(),
                    responseException.getResponseBodyAsString()));
        }
        return compactCause(StringUtils.defaultIfBlank(exception.getMessage(), exception.toString()));
    }

    private String compactCause(final String reason) {
        String singleLine = reason.replaceAll(WHITESPACE_REGEX, StringUtils.SPACE).trim();
        return singleLine.length() > MAX_REASON_LENGTH
                ? singleLine.substring(0, MAX_REASON_LENGTH) + COMPACT_ELLIPSIS
                : singleLine;
    }
}
