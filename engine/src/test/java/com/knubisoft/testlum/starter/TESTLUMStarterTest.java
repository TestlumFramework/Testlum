package com.knubisoft.testlum.starter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

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
        void hasFourValues() {
            assertEquals(4, TESTLUMStarter.ExitCode.values().length);
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
    }
}
