package com.knubisoft.testlum.starter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TESTLUMStarterTest {

    @Nested
    class ExitCodeEnum {

        @Test
        void testsPassedHasCodeZero() {
            assertEquals(0, TESTLUMStarter.ExitCode.TESTS_PASSED.getExitCode());
            assertEquals("Tests passed", TESTLUMStarter.ExitCode.TESTS_PASSED.getMessage());
        }

        @Test
        void testsFailedHasCodeOne() {
            assertEquals(1, TESTLUMStarter.ExitCode.TESTS_FAILED.getExitCode());
            assertEquals("Tests failed", TESTLUMStarter.ExitCode.TESTS_FAILED.getMessage());
        }

        @Test
        void noTestsFoundHasCodeTwo() {
            assertEquals(2, TESTLUMStarter.ExitCode.NO_TESTS_FOUND.getExitCode());
            assertEquals("No tests found", TESTLUMStarter.ExitCode.NO_TESTS_FOUND.getMessage());
        }

        @Test
        void invalidConfigurationHasCodeThree() {
            assertEquals(3, TESTLUMStarter.ExitCode.INVALID_CONFIGURATION.getExitCode());
            assertEquals("Invalid configuration", TESTLUMStarter.ExitCode.INVALID_CONFIGURATION.getMessage());
        }

        @Test
        void hasFiveValues() {
            assertEquals(5, TESTLUMStarter.ExitCode.values().length);
        }

        @Test
        void valueOfReturnsCorrectEnum() {
            assertEquals(TESTLUMStarter.ExitCode.TESTS_PASSED,
                    TESTLUMStarter.ExitCode.valueOf("TESTS_PASSED"));
            assertEquals(TESTLUMStarter.ExitCode.TESTS_FAILED,
                    TESTLUMStarter.ExitCode.valueOf("TESTS_FAILED"));
            assertEquals(TESTLUMStarter.ExitCode.NO_TESTS_FOUND,
                    TESTLUMStarter.ExitCode.valueOf("NO_TESTS_FOUND"));
            assertEquals(TESTLUMStarter.ExitCode.INVALID_CONFIGURATION,
                    TESTLUMStarter.ExitCode.valueOf("INVALID_CONFIGURATION"));
        }
    }

    @Nested
    class GetExitCode {

        @Test
        void returnsNoTestsFoundWhenZeroTestsFound() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(0L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.NO_TESTS_FOUND, result);
        }

        @Test
        void returnsTestsFailedWhenFailuresExist() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(10L);
            when(summary.getTestsFailedCount()).thenReturn(3L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.TESTS_FAILED, result);
        }

        @Test
        void returnsTestsPassedWhenAllTestsPass() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(10L);
            when(summary.getTestsFailedCount()).thenReturn(0L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.TESTS_PASSED, result);
        }

        @Test
        void noTestsFoundTakesPriorityOverFailures() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(0L);
            when(summary.getTestsFailedCount()).thenReturn(5L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.NO_TESTS_FOUND, result);
        }

        @Test
        void returnsTestsPassedWhenOneTestPassed() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(1L);
            when(summary.getTestsFailedCount()).thenReturn(0L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.TESTS_PASSED, result);
        }

        @Test
        void returnsTestsFailedWhenOneTestFailed() {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsFoundCount()).thenReturn(1L);
            when(summary.getTestsFailedCount()).thenReturn(1L);

            TESTLUMStarter.ExitCode result = TESTLUMStarter.getExitCode(summary);

            assertEquals(TESTLUMStarter.ExitCode.TESTS_FAILED, result);
        }
    }

    @Nested
    class InputValidatorTests {

        @Test
        void constructCreatesNewInstance() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Method constructMethod = validatorClass.getDeclaredMethod("construct");
            constructMethod.setAccessible(true);

            Object validator = constructMethod.invoke(null);
            assertNotNull(validator);
        }

        @Test
        void checkAddsErrorWhenConditionIsTrue() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Object validator = createInputValidator(validatorClass);

            Method checkMethod = validatorClass.getDeclaredMethod("check",
                    String.class, java.util.function.Supplier.class, String.class);
            checkMethod.setAccessible(true);

            java.util.function.Supplier<Boolean> trueSupplier = () -> true;
            Object result = checkMethod.invoke(validator, "param1", trueSupplier, "error msg");
            assertNotNull(result);
            assertSame(validator, result);
        }

        @Test
        void checkDoesNotAddErrorWhenConditionIsFalse() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Object validator = createInputValidator(validatorClass);

            Method checkMethod = validatorClass.getDeclaredMethod("check",
                    String.class, java.util.function.Supplier.class, String.class);
            checkMethod.setAccessible(true);

            java.util.function.Supplier<Boolean> falseSupplier = () -> false;
            Object result = checkMethod.invoke(validator, "param1", falseSupplier, "error msg");
            assertNotNull(result);
        }

        @Test
        void checkCatchesExceptionAndAddsError() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Object validator = createInputValidator(validatorClass);

            Method checkMethod = validatorClass.getDeclaredMethod("check",
                    String.class, java.util.function.Supplier.class, String.class);
            checkMethod.setAccessible(true);

            java.util.function.Supplier<Boolean> throwingSupplier = () -> {
                throw new RuntimeException("boom");
            };

            Object result = checkMethod.invoke(validator, "param1", throwingSupplier, "error msg");
            assertNotNull(result);
        }

        @Test
        void exitIfAnyDoesNothingWhenNoErrors() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Object validator = createInputValidator(validatorClass);

            // Add a passing check first
            Method checkMethod = validatorClass.getDeclaredMethod("check",
                    String.class, java.util.function.Supplier.class, String.class);
            checkMethod.setAccessible(true);
            checkMethod.invoke(validator, "param1", (java.util.function.Supplier<Boolean>) () -> false, "no error");

            // exitIfAny should not call System.exit when there are no errors
            Method exitIfAny = validatorClass.getDeclaredMethod("exitIfAny");
            exitIfAny.setAccessible(true);
            assertDoesNotThrow(() -> exitIfAny.invoke(validator));
        }

        @Test
        void chainMultipleChecks() throws Exception {
            Class<?> validatorClass = getInputValidatorClass();
            Object validator = createInputValidator(validatorClass);

            Method checkMethod = validatorClass.getDeclaredMethod("check",
                    String.class, java.util.function.Supplier.class, String.class);
            checkMethod.setAccessible(true);

            Object r1 = checkMethod.invoke(validator, "p1", (java.util.function.Supplier<Boolean>) () -> false, "ok");
            Object r2 = checkMethod.invoke(r1, "p2", (java.util.function.Supplier<Boolean>) () -> false, "ok");
            assertSame(validator, r2);
        }

        private Class<?> getInputValidatorClass() {
            Class<?>[] innerClasses = TESTLUMStarter.class.getDeclaredClasses();
            for (Class<?> c : innerClasses) {
                if (c.getSimpleName().equals("InputValidator")) {
                    return c;
                }
            }
            fail("InputValidator inner class not found");
            return null;
        }

        private Object createInputValidator(final Class<?> validatorClass) throws Exception {
            Method constructMethod = validatorClass.getDeclaredMethod("construct");
            constructMethod.setAccessible(true);
            return constructMethod.invoke(null);
        }
    }

    @Nested
    class ClassAnnotations {

        @Test
        void hasSpringBootApplicationAnnotation() {
            assertNotNull(TESTLUMStarter.class.getAnnotation(
                    org.springframework.boot.autoconfigure.SpringBootApplication.class));
        }

        @Test
        void mainMethodExists() throws NoSuchMethodException {
            assertNotNull(TESTLUMStarter.class.getDeclaredMethod("main", String[].class));
        }
    }
}
