package com.knubisoft.cott.testing.framework.exception;

import com.google.common.collect.Multimap;
import com.knubisoft.cott.testing.framework.util.LogMessage;
import lombok.RequiredArgsConstructor;

public class XSDException extends RuntimeException {

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
            return String.format(LogMessage.XSDISSUE_TO_STRING, message, lineNumber, columnNumber, path);
        }
    }
}
