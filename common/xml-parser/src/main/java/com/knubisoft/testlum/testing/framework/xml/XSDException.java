package com.knubisoft.testlum.testing.framework.xml;

import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class XSDException extends RuntimeException {

    private final transient List<XsdIssue> issues;

    private final String file;

    public XSDException(final File file, final List<XsdIssue> issues) {
        super(XsdIssueFormatter.format(issues));
        this.file = file.getPath();
        this.issues = List.copyOf(issues);
    }

    public XSDException(final File file, final String message, final Throwable cause) {
        super(message, cause);
        this.file = file.getPath();
        this.issues = List.of();
    }
}
