package com.knubisoft.testlum.testing.framework.starter;

import com.knubisoft.testlum.starter.Args;
import com.knubisoft.testlum.starter.Args.Param;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {

    @Nested
    class ConfigFileParam {

        @Test
        void shouldMatchShortFlag() {
            String[] args = {"-c=myconfig.yaml"};
            Optional<String> result = Args.read(args, Param.CONFIG_FILE);
            assertTrue(result.isPresent());
            assertEquals("myconfig.yaml", result.get());
        }

        @Test
        void shouldMatchLongFlag() {
            String[] args = {"--config=myconfig.yaml"};
            Optional<String> result = Args.read(args, Param.CONFIG_FILE);
            assertTrue(result.isPresent());
            assertEquals("myconfig.yaml", result.get());
        }

        @Test
        void shouldReturnEmptyWhenNoMatch() {
            String[] args = {"--path=some/path"};
            Optional<String> result = Args.read(args, Param.CONFIG_FILE);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class PathToTestResourcesParam {

        @Test
        void shouldMatchShortFlag() {
            String[] args = {"-p=/some/path"};
            Optional<String> result = Args.read(args, Param.PATH_TO_TEST_RESOURCES);
            assertTrue(result.isPresent());
            assertEquals("/some/path", result.get());
        }

        @Test
        void shouldMatchLongFlag() {
            String[] args = {"--path=/some/path"};
            Optional<String> result = Args.read(args, Param.PATH_TO_TEST_RESOURCES);
            assertTrue(result.isPresent());
            assertEquals("/some/path", result.get());
        }

        @Test
        void shouldReturnEmptyWhenNoMatch() {
            String[] args = {"-c=config.yaml"};
            Optional<String> result = Args.read(args, Param.PATH_TO_TEST_RESOURCES);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class PathToSpecificScenariosParam {

        @Test
        void shouldMatchShortFlag() {
            String[] args = {"-s=scenario1,scenario2"};
            Optional<String> result = Args.read(args, Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertTrue(result.isPresent());
            assertEquals("scenario1,scenario2", result.get());
        }

        @Test
        void shouldMatchLongFlag() {
            String[] args = {"--scenarios=scenario1,scenario2"};
            Optional<String> result = Args.read(args, Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertTrue(result.isPresent());
            assertEquals("scenario1,scenario2", result.get());
        }

        @Test
        void shouldReturnEmptyWhenNoMatch() {
            String[] args = {"--config=something"};
            Optional<String> result = Args.read(args, Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertFalse(result.isPresent());
        }
    }

    @Test
    void shouldReturnEmptyForEmptyArgsArray() {
        String[] args = {};
        assertFalse(Args.read(args, Param.CONFIG_FILE).isPresent());
        assertFalse(Args.read(args, Param.PATH_TO_TEST_RESOURCES).isPresent());
        assertFalse(Args.read(args, Param.PATH_TO_SPECIFIC_SCENARIOS).isPresent());
    }

    @Test
    void shouldFindCorrectArgAmongMultiple() {
        String[] args = {"-c=config.yaml", "-p=/resources", "-s=scenarios"};
        assertEquals("config.yaml", Args.read(args, Param.CONFIG_FILE).get());
        assertEquals("/resources", Args.read(args, Param.PATH_TO_TEST_RESOURCES).get());
        assertEquals("scenarios", Args.read(args, Param.PATH_TO_SPECIFIC_SCENARIOS).get());
    }

    @Nested
    class ParamEnumValues {

        @Test
        void shouldHaveThreeValues() {
            assertEquals(3, Param.values().length);
        }

        @Test
        void shouldHaveCorrectRegexpForConfigFile() {
            assertNotNull(Param.CONFIG_FILE.getRegexp());
            assertTrue(Param.CONFIG_FILE.getRegexp().contains("-c"));
            assertTrue(Param.CONFIG_FILE.getRegexp().contains("--config"));
        }

        @Test
        void shouldHaveCorrectRegexpForPath() {
            assertNotNull(Param.PATH_TO_TEST_RESOURCES.getRegexp());
            assertTrue(Param.PATH_TO_TEST_RESOURCES.getRegexp().contains("-p"));
            assertTrue(Param.PATH_TO_TEST_RESOURCES.getRegexp().contains("--path"));
        }

        @Test
        void shouldHaveCorrectRegexpForScenarios() {
            assertNotNull(Param.PATH_TO_SPECIFIC_SCENARIOS.getRegexp());
            assertTrue(Param.PATH_TO_SPECIFIC_SCENARIOS.getRegexp().contains("-s"));
            assertTrue(Param.PATH_TO_SPECIFIC_SCENARIOS.getRegexp().contains("--scenarios"));
        }
    }
}
