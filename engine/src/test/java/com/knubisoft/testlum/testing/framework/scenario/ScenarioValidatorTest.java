package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.framework.xml.XMLValidator;
import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ScenarioValidator} verifying class structure
 * and interface implementation.
 */
class ScenarioValidatorTest {

    @Nested
    class ClassStructure {

        @Test
        void implementsXMLValidator() {
            assertTrue(XMLValidator.class.isAssignableFrom(ScenarioValidator.class));
        }

        @Test
        void classIsAnnotatedWithComponent() {
            assertNotNull(ScenarioValidator.class.getAnnotation(
                    org.springframework.stereotype.Component.class));
        }

        @Test
        void initMethodExists() throws NoSuchMethodException {
            assertNotNull(ScenarioValidator.class.getDeclaredMethod("init"));
        }

        @Test
        void validateMethodExists() throws NoSuchMethodException {
            assertNotNull(ScenarioValidator.class.getDeclaredMethod("validate",
                    Scenario.class, java.io.File.class));
        }
    }
}
