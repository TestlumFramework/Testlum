package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.exception.ComparisonException;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CompareBuilder} verifying fluent comparison API,
 * strict/lenient modes, and file persistence on mismatch.
 */
class CompareBuilderTest {

    @TempDir
    File tempDir;

    private JacksonService jacksonService;
    private StringPrettifier stringPrettifier;
    private CompareBuilder builder;

    @BeforeEach
    void setUp() {
        jacksonService = mock(JacksonService.class);
        stringPrettifier = mock(StringPrettifier.class);
        when(stringPrettifier.prettify(anyString()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(stringPrettifier.prettifyToSave(anyString()))
                .thenAnswer(inv -> inv.getArgument(0));

        final File scenarioFile = new File(tempDir, "scenario.xml");
        builder = new CompareBuilder(scenarioFile, 1, jacksonService, stringPrettifier);
    }

    @Nested
    class ExecWithNullExpected {
        @Test
        void skipsComparisonWhenExpectedIsNull() {
            builder.withActual("anything");
            assertDoesNotThrow(() -> builder.exec());
        }
    }

    @Nested
    class ExecWithMatchingContent {
        @Test
        void matchingStringsDoNotThrow() {
            builder.withExpected("hello world")
                    .withActual("hello world");
            assertDoesNotThrow(() -> builder.exec());
        }

        @Test
        void matchingJsonDoesNotThrow() {
            builder.withExpected("{\"a\":1}")
                    .withActual("{\"a\":1}");
            assertDoesNotThrow(() -> builder.exec());
        }
    }

    @Nested
    class ExecWithMismatch {
        @Test
        void mismatchThrowsComparisonException() {
            builder.withExpected("{\"a\":1}")
                    .withActual("{\"a\":2}");
            assertThrows(ComparisonException.class, () -> builder.exec());
        }

        @Test
        void mismatchSavesActualToFile() {
            builder.withExpected("{\"a\":1}")
                    .withActual("{\"a\":2}");
            try {
                builder.exec();
            } catch (ComparisonException ignored) {
                // expected
            }
            final File saved = new File(tempDir, "action_1_actual.json");
            assertTrue(saved.exists());
        }
    }

    @Nested
    class FluentApi {
        @Test
        void withModeSetsFlagAndReturnsBuilder() {
            final CompareBuilder result = builder.withMode(false);
            assertTrue(result == builder);
        }

        @Test
        void withActualObjectUsesJacksonService() {
            final Object obj = new Object();
            when(jacksonService.writeValueAsString(obj)).thenReturn("{}");
            builder.withExpected("{}").withActual(obj);
            assertDoesNotThrow(() -> builder.exec());
        }

        @Test
        void lenientModeAllowsExtraFields() {
            builder.withExpected("{\"a\":1}")
                    .withActual("{\"a\":1,\"b\":2}")
                    .withMode(false);
            assertDoesNotThrow(() -> builder.exec());
        }

        @Test
        void strictModeRejectsExtraFields() {
            builder.withExpected("{\"a\":1}")
                    .withActual("{\"a\":1,\"b\":2}")
                    .withMode(true);
            assertThrows(ComparisonException.class, () -> builder.exec());
        }
    }

    @Nested
    class WithExpectedFile {
        @Test
        void withExpectedFileReadsContent() throws java.io.IOException {
            final File expectedFile = new File(tempDir, "expected.json");
            org.apache.commons.io.FileUtils.writeStringToFile(
                    expectedFile, "{\"a\":1}", java.nio.charset.StandardCharsets.UTF_8);

            final com.knubisoft.testlum.testing.framework.FileSearcher fileSearcher =
                    mock(com.knubisoft.testlum.testing.framework.FileSearcher.class);
            when(fileSearcher.searchFileFromDir(
                    new File(tempDir, "scenario.xml"), "expected.json"))
                    .thenReturn(expectedFile);

            builder.withExpectedFile(fileSearcher, "expected.json")
                    .withActual("{\"a\":1}");
            assertDoesNotThrow(() -> builder.exec());
        }

        @Test
        void withExpectedFileBlankNameDoesNothing() {
            final com.knubisoft.testlum.testing.framework.FileSearcher fileSearcher =
                    mock(com.knubisoft.testlum.testing.framework.FileSearcher.class);
            builder.withExpectedFile(fileSearcher, "");
            // expected is still null, so exec should skip comparison
            builder.withActual("anything");
            assertDoesNotThrow(() -> builder.exec());
        }
    }

    @Nested
    class Position {
        @Test
        void builderStoresPosition() {
            final File scenarioFile = new File(tempDir, "test.xml");
            final CompareBuilder b = new CompareBuilder(scenarioFile, 42, jacksonService, stringPrettifier);
            assertTrue(b.getPosition() == 42);
        }
    }
}
