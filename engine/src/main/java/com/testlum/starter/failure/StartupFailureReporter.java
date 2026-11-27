package com.testlum.starter.failure;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.FormattedFailure;
import com.testlum.testing.framework.xml.XSDException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
public final class StartupFailureReporter {

    private static final String ADDITIONAL_INFO = "Full stacktrace:";

    private static final Set<Throwable> REPORTED =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

    private StartupFailureReporter() {
    }

    public static void report(final String headline, final Throwable failure) {
        if (failure == null) {
            return;
        }
        REPORTED.add(rootCauseOf(failure));
        log.error(LogFormat.withRed(constructReport(headline, failure)));
    }

    public static boolean wasReported(final Throwable failure) {
        return failure != null && REPORTED.contains(rootCauseOf(failure));
    }

    private static String constructReport(final String headline, final Throwable failure) {
        FormattedFailure formatted = FormattedFailure.findIn(failure);
        Throwable rootCause = rootCauseOf(failure);
        String newLine = System.lineSeparator();
        return newLine + LogMessage.LINE + newLine
                + headline + newLine
               + describe(formatted, rootCause) + newLine
               + stacktraceOf(formatted, failure, rootCause)
                + LogMessage.LINE;
    }

    private static String stacktraceOf(final FormattedFailure formatted,
                                       final Throwable failure,
                                       final Throwable rootCause) {
        if (formatted != null || rootCause instanceof XSDException) {
            return StringUtils.EMPTY;
        }
        String newLine = System.lineSeparator();
        return newLine + ADDITIONAL_INFO + newLine + toString(failure::printStackTrace);
    }

    private static String describe(final FormattedFailure formatted, final Throwable rootCause) {
        if (formatted != null) {
            return formatted.describe();
        }
        if (rootCause instanceof XSDException schemaFailure) {
            return schemaFailure.getFile() + System.lineSeparator() + schemaFailure.getMessage();
        }
        String message = rootCause.getMessage();
        return StringUtils.isBlank(message) ? rootCause.getClass().getSimpleName() : message;
    }

    private static Throwable rootCauseOf(final Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    private static String toString(final Consumer<PrintWriter> writer) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        writer.accept(printWriter);
        return stringWriter.toString();
    }

}
