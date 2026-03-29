package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioArgumentsToNamedConverterTest {

    private final ScenarioArgumentsToNamedConverter converter = new ScenarioArgumentsToNamedConverter();

    @Nested
    class Convert {
        @Test
        void convertsScenarioArgumentsToNamed() {
            final ScenarioArguments args = ScenarioArguments.builder()
                    .path("scenarios/login.xml")
                    .build();

            final Object result = converter.convert((Object) args, (ParameterContext) null);

            assertInstanceOf(Named.class, result);
            final Named<?> named = (Named<?>) result;
            assertEquals("scenarios/login.xml", named.getName());
            assertEquals(args, named.getPayload());
        }

        @Test
        void throwsForNonScenarioArguments() {
            assertThrows(DefaultFrameworkException.class,
                    () -> converter.convert((Object) "not a scenario", (ParameterContext) null));
        }

        @Test
        void throwsForNull() {
            assertThrows(DefaultFrameworkException.class,
                    () -> converter.convert((Object) null, (ParameterContext) null));
        }
    }
}
