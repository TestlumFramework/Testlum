package com.knubisoft.e2e.testing.framework.parser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.exception.XSDException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

@Getter
@Slf4j
@UtilityClass
public final class XSDValidator {

    public static void validateBySchema(final File file, final Schema schema) {
        Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();

        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandlerImpl(errors, file));

        tryToValidate(file, validator);

        if (!errors.isEmpty()) {
            throw new XSDException(errors);
        }
    }

    private static void tryToValidate(final File file, final Validator validator) {
        try {
            validator.validate(new StreamSource(file));
        } catch (SAXException | IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private static void collect(final Multimap<String, XSDException.XSDIssue> map,
                                final String level,
                                final SAXParseException e,
                                final File file) {
        map.put(level, new XSDException.XSDIssue(e.getMessage(),
                e.getLineNumber(),
                e.getColumnNumber(),
                file.getAbsolutePath()));
    }

    @RequiredArgsConstructor
    private static class ErrorHandlerImpl implements ErrorHandler {
        private static final String WARN_LEVEL = "WARNING";
        private static final String ERROR_LEVEL = "ERROR";
        private static final String FATAL_ERR_LEVEL = "FATAL_ERROR";

        private final Multimap<String, XSDException.XSDIssue> errors;
        private final File file;

        @Override
        public void warning(final SAXParseException e) {
            collect(errors, WARN_LEVEL, e, file);
        }

        @Override
        public void error(final SAXParseException e) {
            collect(errors, ERROR_LEVEL, e, file);
        }

        @Override
        public void fatalError(final SAXParseException e) {
            collect(errors, FATAL_ERR_LEVEL, e, file);
        }
    }
}
