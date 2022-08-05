package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.framework.exception.ComparisonException;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.FileLinkingException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.framework.util.StringPrettifier;
import com.knubisoft.cott.testing.framework.util.TreeComparator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMENT_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMPARISON_FOR_STEP_WAS_SKIPPED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.POSITION_COMMAND_LOG;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SLOW_COMMAND_PROCESSING;
import static java.lang.String.format;

@Slf4j
public abstract class AbstractInterpreter<T extends AbstractCommand> {

    protected final InterpreterDependencies dependencies;

    protected AbstractInterpreter(final InterpreterDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        log.info(format(POSITION_COMMAND_LOG, dependencies.getPosition().get(), o.getClass().getSimpleName()));
        if (StringUtils.isNotBlank(o.getComment())) {
            log.info(COMMENT_LOG, o.getComment());
        }
        checkExecutionTime(o, () -> acceptImpl(o, result));
    }

    private void checkExecutionTime(final T o, final Runnable r) {
        StopWatch sw = StopWatch.createStarted();
        r.run();
        long ms = sw.getTime(TimeUnit.MILLISECONDS);
        Integer threshold = o.getThreshold();
        if (o.getThreshold() != null && ms > threshold) {
            throw new DefaultFrameworkException(SLOW_COMMAND_PROCESSING, ms, threshold);
        }
    }

    protected abstract void acceptImpl(T o, CommandResult result);

    public void save(final String actual) {
        try {
            File target = new File(dependencies.getFile().getParent(),
                    String.format(TestResourceSettings.FILENAME_TO_SAVE, dependencies.getPosition().get()));
            FileUtils.writeStringToFile(target, prettify(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }

    @SneakyThrows
    public String toString(final Object o) {
        return JacksonMapperUtil.writeValueAsString(o);
    }

    @SneakyThrows
    protected void setContextBody(final String o) {
        dependencies.getScenarioContext().setBody(o);
    }

    private String prettify(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception ignore) {
            return actual;
        }
    }

    protected String getContentIfFile(final String fileOrContent) {
        try {
            if (fileOrContent.endsWith(".json")) {
                return FileSearcher.searchFileToString(fileOrContent, dependencies.getFile());
            }
        } catch (FileLinkingException e) {
            // pass
        }
        return fileOrContent;

    }

    private String tryToPrettify(final String actual) {
        if (actual.startsWith(OPEN_BRACE) || actual.startsWith(OPEN_SQUARE_BRACKET)) {
            Object json = JacksonMapperUtil.readValue(actual, Object.class);
            return JacksonMapperUtil.writeValueAsStringWithDefaultPrettyPrinter(json);
        }
        return actual;
    }

    public String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    protected CompareBuilder newCompare() {
        return new CompareBuilder();
    }

    @Getter
    protected class CompareBuilder {
        private String expected;
        private Supplier<String> supplierActual;

        public CompareBuilder withActual(final Object actual) {
            this.supplierActual = () -> AbstractInterpreter.this.toString(actual);
            return this;
        }

        public CompareBuilder withActual(final String actual) {
            this.supplierActual = () -> String.valueOf(actual);
            return this;
        }

        public CompareBuilder withExpected(final String expected) {
            this.expected = String.valueOf(expected);
            return this;
        }

        public CompareBuilder withExpectedFile(final String fileName) {
            if (fileName != null) {
                File file = FileSearcher.searchFileFromDir(dependencies.getFile(), fileName);
                return tryToUseExpectedFile(file);
            }
            return this;
        }

        private CompareBuilder tryToUseExpectedFile(final File file) {
            try {
                return withExpected(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new DefaultFrameworkException(e);
            }
        }

        @SneakyThrows
        public void exec() {
            if (expected != null) {
                String actual = supplierActual.get();
                tryToCompare(actual);
            } else {
                log.info(format(COMPARISON_FOR_STEP_WAS_SKIPPED, dependencies.getPosition()));
            }
        }

        private void tryToCompare(final String actual) {
            try {
                final String newActual = StringPrettifier.prettify(actual);
                final String newExpected = StringPrettifier.prettify(expected);
                TreeComparator.compare(newExpected, newActual);
            } catch (ComparisonException e) {
                save(actual);
                throw new DefaultFrameworkException(e);
            }
        }
    }
}
