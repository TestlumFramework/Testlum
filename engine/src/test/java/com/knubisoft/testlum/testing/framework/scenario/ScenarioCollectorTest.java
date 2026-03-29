package com.knubisoft.testlum.testing.framework.scenario;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScenarioCollector} verifying inner classes
 * and result structure.
 */
class ScenarioCollectorTest {

    @Nested
    class ResultStructure {

        @Test
        void emptyResultCreation() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void addMappingResultToResult() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            final File file = new File("scenario.xml");
            result.add(new ScenarioCollector.MappingResult(file, null, null));

            assertEquals(1, result.size());
            assertEquals(file, result.get(0).file);
        }

        @Test
        void multipleResultsCanBeAdded() {
            final ScenarioCollector.Result result = new ScenarioCollector.Result();
            result.add(new ScenarioCollector.MappingResult(new File("a.xml"), null, null));
            result.add(new ScenarioCollector.MappingResult(new File("b.xml"), null, null));
            result.add(new ScenarioCollector.MappingResult(new File("c.xml"), null, null));

            assertEquals(3, result.size());
        }
    }

    @Nested
    class MappingResultFields {

        @Test
        void mappingResultHoldsFileAndException() {
            final File file = new File("test.xml");
            final Exception exception = new RuntimeException("error");
            final ScenarioCollector.MappingResult mappingResult =
                    new ScenarioCollector.MappingResult(file, null, exception);

            assertEquals(file, mappingResult.file);
            assertNull(mappingResult.scenario);
            assertNotNull(mappingResult.exception);
            assertEquals("error", mappingResult.exception.getMessage());
        }

        @Test
        void mappingResultWithNullException() {
            final File file = new File("valid.xml");
            final ScenarioCollector.MappingResult mappingResult =
                    new ScenarioCollector.MappingResult(file, null, null);

            assertEquals(file, mappingResult.file);
            assertNull(mappingResult.exception);
        }

        @Test
        void mappingResultFieldsAreFinal() {
            final File file = new File("scenario.xml");
            final RuntimeException exception = new RuntimeException("test");
            final ScenarioCollector.MappingResult result =
                    new ScenarioCollector.MappingResult(file, null, exception);

            assertEquals("scenario.xml", result.file.getName());
            assertEquals("test", result.exception.getMessage());
        }
    }
}
