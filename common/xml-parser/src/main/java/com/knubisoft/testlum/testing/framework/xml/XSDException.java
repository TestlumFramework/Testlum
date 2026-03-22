package com.knubisoft.testlum.testing.framework.xml;

import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;

public class XSDException extends RuntimeException {

    private static final String XSDISSUE_TO_STRING =
            "XSDIssue{message='%s', lineNumber=%d, columnNumber=%d, path=%s}";

    public XSDException(final Multimap<String, XSDIssue> errors) {
        super(errors.toString());
    }

    @RequiredArgsConstructor
    public static class XSDIssue {

        public final String message;
        public final int lineNumber;
        public final int columnNumber;
        public final String path;

        @Override
        public String toString() {
            return String.format(XSDISSUE_TO_STRING, message, lineNumber, columnNumber, path);
        }
    }
}
