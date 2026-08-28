package com.testlum.testing.framework.xml;

import com.testlum.testing.framework.constant.ExceptionMessage;
import com.testlum.testing.framework.constant.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class XSDValidator {

    public static void validateBySchema(final File file, final Schema schema) {
        List<XsdIssue> issues = new ArrayList<>();

        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandlerImpl(issues));

        tryToValidate(file, validator, issues);

        logWarnings(file, issues);
        if (hasBlockingIssues(issues)) {
            throw new XSDException(file, issues);
        }
    }

    private static void tryToValidate(final File file,
                                      final Validator validator,
                                      final List<XsdIssue> issues) {
        try {
            validator.validate(new StreamSource(file));
        } catch (SAXException | IOException e) {
            if (hasBlockingIssues(issues)) {
                throw new XSDException(file, issues);
            }
            throw new XSDException(file,
                    String.format(ExceptionMessage.XSD_FILE_NOT_READABLE, file.getName(), e.getMessage()), e);
        }
    }

    private static void logWarnings(final File file, final List<XsdIssue> issues) {
        issues.stream()
                .filter(issue -> !issue.blocksValidation())
                .forEach(issue -> log.warn(LogMessage.XSD_SCHEMA_WARNING_LOG,
                        file.getName(), issue.line(), issue.messageWithoutCode()));
    }

    private static boolean hasBlockingIssues(final List<XsdIssue> issues) {
        return issues.stream().anyMatch(XsdIssue::blocksValidation);
    }

    @RequiredArgsConstructor
    private static class ErrorHandlerImpl implements ErrorHandler {

        private final List<XsdIssue> issues;

        @Override
        public void warning(final SAXParseException e) {
            issues.add(XsdIssue.of(XsdSeverity.WARNING, e));
        }

        @Override
        public void error(final SAXParseException e) {
            issues.add(XsdIssue.of(XsdSeverity.ERROR, e));
        }

        @Override
        public void fatalError(final SAXParseException e) {
            issues.add(XsdIssue.of(XsdSeverity.FATAL, e));
        }
    }
}
