package com.knubisoft.testlum.starter.failure;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.xml.XSDException;
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
        Throwable rootCause = rootCauseOf(failure);
        String newLine = System.lineSeparator();
        return newLine + LogMessage.LINE + newLine
                + headline + newLine
                + describe(rootCause) + newLine
                + stacktraceOf(failure, rootCause)
                + LogMessage.LINE;
    }

    private static String stacktraceOf(final Throwable failure, final Throwable rootCause) {
        if (rootCause instanceof XSDException) {
            return StringUtils.EMPTY;
        }
        String newLine = System.lineSeparator();
        return newLine + ADDITIONAL_INFO + newLine + toString(failure::printStackTrace);
    }

    private static String describe(final Throwable rootCause) {
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
