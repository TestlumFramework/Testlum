package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.comparator.Comparator;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.ExpectedFileUtils;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

@Slf4j
@Getter
public class CompareBuilder {

    private static final String COMPARISON_FOR_STEP_WAS_SKIPPED = "Comparison for step [{}] was skipped";

    private final File scenarioFile;
    private final int position;
    private final JacksonService jacksonService;
    private final StringPrettifier stringPrettifier;

    private String expected;
    private String expectedFileName;
    private Supplier<String> supplierActual;
    private boolean isStrict;

    public CompareBuilder(final File scenarioFile,
                          final int position,
                          final JacksonService jacksonService,
                          final StringPrettifier stringPrettifier) {
        this.scenarioFile = scenarioFile;
        this.position = position;
        this.isStrict = true;
        this.jacksonService = jacksonService;
        this.stringPrettifier = stringPrettifier;
    }

    public CompareBuilder withActual(final Object actual) {
        this.supplierActual = () -> jacksonService.writeValueAsString(actual);
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

    public CompareBuilder withExpectedFile(final FileSearcher fileSearcher, final String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            File file = fileSearcher.searchFileFromDir(scenarioFile, fileName);
            return tryToUseExpectedFile(file);
        }
        return this;
    }

    public CompareBuilder withMode(final boolean isStrict) {
        this.isStrict = isStrict;
        return this;
    }

    public CompareBuilder withExpectedFileName(final String fileName) {
        if (StringUtils.isNotBlank(fileName)) {
            this.expectedFileName = fileName;
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

    public void exec() {
        if (Objects.nonNull(expected)) {
            String actual = supplierActual.get();
            tryToCompare(actual);
        } else {
            log.info(COMPARISON_FOR_STEP_WAS_SKIPPED, position);
        }
    }

    private void tryToCompare(final String actual) {
        try {
            final String newActual = stringPrettifier.prettify(actual);
            final String newExpected = stringPrettifier.prettify(expected);

            new TreeComparator(isStrict()).compare(newExpected, newActual);
        } catch (ComparisonException e) {
            save(actual);
            throw e;
        }
    }

    private void save(final String actual) {
        try {
            String actualFileNameWithExecutionStep =
                    ExpectedFileUtils.resolveActualNameBasedOnExpectedFileName(this.expectedFileName, position);
            File target = new File(this.scenarioFile.getParent(), actualFileNameWithExecutionStep);
            FileUtils.writeStringToFile(target, stringPrettifier.prettifyToSave(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }

    @RequiredArgsConstructor
    private static final class TreeComparator {

        private final boolean strict;

        public void compare(final String expected, final String actual) throws ComparisonException {
            try {
                Comparator comparator = strict ? Comparator.strict() : Comparator.lenient();
                comparator.compare(expected, actual);
            } catch (Throwable t) {
                throw new ComparisonException(t.getMessage());
            }
        }

    }
}
