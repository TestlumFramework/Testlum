package com.knubisoft.testlum.testing.framework.constant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link ExceptionMessage} constants. */
class ExceptionMessageTest {

    @Nested
    class ConfigFilenames {
        @Test
        void integrationConfigFilename() {
            assertEquals("integration.xml", ExceptionMessage.INTEGRATION_CONFIG_FILENAME);
        }

        @Test
        void uiConfigFilename() {
            assertEquals("ui.xml", ExceptionMessage.UI_CONFIG_FILENAME);
        }
    }

    @Nested
    class InterpreterMessages {
        @Test
        void notDeclaredWithInterpreterForClassContainsPlaceholder() {
            assertTrue(ExceptionMessage.NOT_DECLARED_WITH_INTERPRETER_FOR_CLASS.contains("%s"));
        }

        @Test
        void functionForCommandNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.FUNCTION_FOR_COMMAND_NOT_FOUND.contains("%s"));
        }

        @Test
        void interpreterNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.INTERPRETER_NOT_FOUND.contains("%s"));
        }
    }

    @Nested
    class ExecutorMessages {
        @Test
        void notDeclaredWithExecutorForClassContainsPlaceholder() {
            assertTrue(ExceptionMessage.NOT_DECLARED_WITH_EXECUTOR_FOR_CLASS.contains("%s"));
        }

        @Test
        void executorForUiCommandNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.EXECUTOR_FOR_UI_COMMAND_NOT_FOUND.contains("%s"));
        }
    }

    @Nested
    class AliasMessages {
        @Test
        void aliasByStorageNameNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.ALIAS_BY_STORAGE_NAME_NOT_FOUND.contains("%s"));
        }

        @Test
        void aliasNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.ALIAS_NOT_FOUND.contains("%s"));
        }
    }

    @Nested
    class EnvironmentMessages {
        @Test
        void noEnabledEnvironmentsFoundIsNotEmpty() {
            assertNotNull(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND);
            assertFalse(ExceptionMessage.NO_ENABLED_ENVIRONMENTS_FOUND.isEmpty());
        }

        @Test
        void noEnabledReportGeneratorsFoundIsNotEmpty() {
            assertNotNull(ExceptionMessage.NO_ENABLED_REPORT_GENERATORS_FOUND);
            assertFalse(ExceptionMessage.NO_ENABLED_REPORT_GENERATORS_FOUND.isEmpty());
        }
    }

    @Nested
    class ScenarioMessages {
        @Test
        void scenarioCannotBeIncludedToItself() {
            assertEquals("Scenario cannot be included to itself",
                    ExceptionMessage.SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF);
        }

        @Test
        void stopIfNonParsedScenarioIsNotEmpty() {
            assertNotNull(ExceptionMessage.STOP_IF_NON_PARSED_SCENARIO);
        }

        @Test
        void validScenariosNotFoundIsNotEmpty() {
            assertNotNull(ExceptionMessage.VALID_SCENARIOS_NOT_FOUND);
        }
    }

    @Nested
    class IntegrationMessages {
        @Test
        void integrationNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.INTEGRATION_NOT_FOUND.contains("%s"));
        }

        @Test
        void dbNotSupportedContainsPlaceholder() {
            assertTrue(ExceptionMessage.DB_NOT_SUPPORTED.contains("%s"));
        }

        @Test
        void unsupportedMigrationFormatContainsPlaceholder() {
            assertTrue(ExceptionMessage.UNSUPPORTED_MIGRATION_FORMAT.contains("%s"));
        }
    }

    @Nested
    class RedisMessages {
        @Test
        void redisCommandNotFoundIsNotEmpty() {
            assertNotNull(ExceptionMessage.REDIS_COMMAND_NOT_FOUND);
            assertEquals("Redis command was not provided", ExceptionMessage.REDIS_COMMAND_NOT_FOUND);
        }
    }

    @Nested
    class DriverMessages {
        @Test
        void driverInitializerNotFound() {
            assertEquals("Driver initializer not found", ExceptionMessage.DRIVER_INITIALIZER_NOT_FOUND);
        }

        @Test
        void webDriverNotInitContainsCheckConfig() {
            assertTrue(ExceptionMessage.WEB_DRIVER_NOT_INIT.contains("check your configuration"));
        }

        @Test
        void mobilebrowserDriverNotInitContainsCheckConfig() {
            assertTrue(ExceptionMessage.MOBILEBROWSER_DRIVER_NOT_INIT.contains("check your configuration"));
        }

        @Test
        void nativeDriverNotInitContainsCheckConfig() {
            assertTrue(ExceptionMessage.NATIVE_DRIVER_NOT_INIT.contains("check your configuration"));
        }
    }

    @Nested
    class VariableMessages {
        @Test
        void varTypeNotSupportedContainsPlaceholder() {
            assertTrue(ExceptionMessage.VAR_TYPE_NOT_SUPPORTED.contains("%s"));
        }

        @Test
        void varQueryResultErrorIsNotEmpty() {
            assertNotNull(ExceptionMessage.VAR_QUERY_RESULT_ERROR);
            assertFalse(ExceptionMessage.VAR_QUERY_RESULT_ERROR.isEmpty());
        }

        @Test
        void envVariableNotFoundContainsPlaceholder() {
            assertTrue(ExceptionMessage.ENV_VARIABLE_NOT_FOUND.contains("%s"));
        }
    }

    @Nested
    class UiMessages {
        @Test
        void imagesSizeMismatchContainsPlaceholders() {
            assertTrue(ExceptionMessage.IMAGES_SIZE_MISMATCH.contains("%s"));
        }

        @Test
        void imagesMismatchContainsPlaceholder() {
            assertTrue(ExceptionMessage.IMAGES_MISMATCH.contains("%s"));
        }

        @Test
        void tabNotFoundIsNotEmpty() {
            assertNotNull(ExceptionMessage.TAB_NOT_FOUND);
        }

        @Test
        void tabOutOfBoundsContainsPlaceholder() {
            assertTrue(ExceptionMessage.TAB_OUT_OF_BOUNDS.contains("%s"));
        }
    }

    @Nested
    class FormattingVerification {
        @Test
        void formatWithValidArgs() {
            String result = String.format(ExceptionMessage.INTEGRATION_NOT_FOUND, "Elasticsearch");
            assertEquals("Cannot find integration configuration for <Elasticsearch>", result);
        }

        @Test
        void formatAliasNotFound() {
            String result = String.format(ExceptionMessage.ALIAS_NOT_FOUND, "my-alias");
            assertEquals("Cannot find enabled integration with alias <my-alias>", result);
        }

        @Test
        void formatDbNotSupported() {
            String result = String.format(ExceptionMessage.DB_NOT_SUPPORTED, "CouchDB");
            assertEquals("Database by name CouchDB not supported", result);
        }
    }
}
