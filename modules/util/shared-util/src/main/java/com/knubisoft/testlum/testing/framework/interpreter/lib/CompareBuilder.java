package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.FileSearcher;
import com.knubisoft.testlum.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.framework.util.TreeComparator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Slf4j
@Getter
public class CompareBuilder {

    private static final String COMPARISON_FOR_STEP_WAS_SKIPPED = "Comparison for step [{}] was skipped";
    private static final String ACTUAL_FILENAME = "actual.json";
    private static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;
    private final File scenarioFile;
    private final int position;
    private String expected;
    private Supplier<String> supplierActual;

    public CompareBuilder(final File scenarioFile, final int position) {
        this.scenarioFile = scenarioFile;
        this.position = position;
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

    private CompareBuilder tryToUseExpectedFile(final File file) {
        try {
            return withExpected(FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public void exec() {
        //must be nonNull
        if (nonNull(expected)) {
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
            TreeComparator.compare(newExpected, newActual);
        } catch (ComparisonException e) {
            save(actual);
            throw e;
        }
    }

    private void save(final String actual) {
        try {
            File target = new File(scenarioFile.getParent(), format(FILENAME_TO_SAVE, position));
            FileUtils.writeStringToFile(target, StringPrettifier.prettifyToSave(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }
}
