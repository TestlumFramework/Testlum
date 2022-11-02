package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.ComparisonException;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.JacksonMapperUtil;
import com.knubisoft.cott.testing.framework.util.StringPrettifier;
import com.knubisoft.cott.testing.framework.util.TreeComparator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMPARISON_FOR_STEP_WAS_SKIPPED;
import static java.lang.String.format;

@Slf4j
@Getter
public class CompareBuilder {

    private final File scenarioFile;
    private final AtomicInteger position;
    private String expected;
    private Supplier<String> supplierActual;

    public CompareBuilder(final File scenarioFile,
                          final AtomicInteger position) {
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
        if (fileName != null) {
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

    @SneakyThrows
    public void exec() {
        if (expected != null) {
            String actual = supplierActual.get();
            tryToCompare(actual);
        } else {
            log.info(format(COMPARISON_FOR_STEP_WAS_SKIPPED, position));
        }
    }

    private void tryToCompare(final String actual) {
        try {
            final String newActual = StringPrettifier.prettify(actual);
            final String newExpected = StringPrettifier.prettify(expected);
            TreeComparator.compare(newExpected, newActual);
        } catch (ComparisonException e) {
           save(actual);
        }
    }

    private String prettify(final String actual) {
        try {
            return tryToPrettify(actual);
        } catch (Exception ignore) {
            return actual;
        }
    }

    private void save(final String actual) {
        try {
            File target = new File(scenarioFile.getParent(),
                    String.format(TestResourceSettings.FILENAME_TO_SAVE, position.get()));
            FileUtils.writeStringToFile(target, prettify(actual), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DefaultFrameworkException(e);
        }
    }
    private String tryToPrettify(final String actual) {
        if (actual.startsWith(OPEN_BRACE) || actual.startsWith(OPEN_SQUARE_BRACKET)) {
            Object json = JacksonMapperUtil.readValue(actual, Object.class);
            return JacksonMapperUtil.writeValueAsStringWithDefaultPrettyPrinter(json);
        }
        return actual;
    }
}
