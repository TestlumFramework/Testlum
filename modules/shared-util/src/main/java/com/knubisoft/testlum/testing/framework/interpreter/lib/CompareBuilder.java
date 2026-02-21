package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.comparator.Comparator;
import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import lombok.Getter;
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
    private static final String FILENAME_FORMAT_TO_SAVE = "action_%s_actual.json";

    private final File scenarioFile;
    private final int position;
    private String expected;
    private Supplier<String> supplierActual;
    private boolean isStrict;

    public CompareBuilder(final File scenarioFile, final int position) {
        this.scenarioFile = scenarioFile;
        this.position = position;
        this.isStrict = true;
    }

    public CompareBuilder withActual(final Object actual) {
        this.supplierActual = () -> JacksonMapperUtil.writeValueAsString(actual);
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
        if (StringUtils.isNotBlank(fileName)) {
            File file = FileSearcher.searchFileFromDir(scenarioFile, fileName);
            return tryToUseExpectedFile(file);
        }
        return this;
    }

    public CompareBuilder withMode(final boolean isStrict) {
        this.isStrict = isStrict;
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
            final String newActual = StringPrettifier.prettify(actual);
            final String newExpected = StringPrettifier.prettify(expected);

            new TreeComparator(isStrict()).compare(newExpected, newActual);
        } catch (ComparisonException e) {
            save(actual);
            throw e;
        }
    }

    private void save(final String actual) {
        try {
            File target = new File(scenarioFile.getParent(), String.format(FILENAME_FORMAT_TO_SAVE, position));
            FileUtils.writeStringToFile(target, StringPrettifier.prettifyToSave(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }

    private static final class TreeComparator {

        private final Comparator comparator;

        TreeComparator(final boolean strict) {
            this.comparator = strict ? Comparator.strict() : Comparator.lenient();
        }

        public void compare(final String expected, final String actual) throws ComparisonException {
            try {
                comparator.compare(expected, actual);
            } catch (Throwable t) {
                throw new ComparisonException(t.getMessage());
            }
        }

    }
}
