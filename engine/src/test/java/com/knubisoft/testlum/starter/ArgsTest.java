package com.knubisoft.testlum.starter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {

    @Nested
    class ConfigFile {

        @Test
        void parsesShortFlag() {
            String[] args = {"-c=config.xml"};
            Optional<String> result = Args.read(args, Args.Param.CONFIG_FILE);
            assertTrue(result.isPresent());
            assertEquals("config.xml", result.get());
        }

        @Test
        void parsesLongFlag() {
            String[] args = {"--config=global-config.xml"};
            Optional<String> result = Args.read(args, Args.Param.CONFIG_FILE);
            assertTrue(result.isPresent());
            assertEquals("global-config.xml", result.get());
        }

        @Test
        void returnsEmptyWhenNotPresent() {
            String[] args = {"-p=/some/path"};
            Optional<String> result = Args.read(args, Args.Param.CONFIG_FILE);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class PathToTestResources {

        @Test
        void parsesShortFlag() {
            String[] args = {"-p=/home/user/resources"};
            Optional<String> result = Args.read(args, Args.Param.PATH_TO_TEST_RESOURCES);
            assertTrue(result.isPresent());
            assertEquals("/home/user/resources", result.get());
        }

        @Test
        void parsesLongFlag() {
            String[] args = {"--path=/opt/test"};
            Optional<String> result = Args.read(args, Args.Param.PATH_TO_TEST_RESOURCES);
            assertTrue(result.isPresent());
            assertEquals("/opt/test", result.get());
        }
    }

    @Nested
    class ScenarioScope {

        @Test
        void parsesShortFlag() {
            String[] args = {"-s=login-scenario"};
            Optional<String> result = Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertTrue(result.isPresent());
            assertEquals("login-scenario", result.get());
        }

        @Test
        void parsesLongFlag() {
            String[] args = {"--scenarios=smoke-tests"};
            Optional<String> result = Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertTrue(result.isPresent());
            assertEquals("smoke-tests", result.get());
        }

        @Test
        void returnsEmptyWhenNotProvided() {
            String[] args = {"-c=config.xml", "-p=/path"};
            Optional<String> result = Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class MultipleArgs {

        @Test
        void parsesAllArgsFromCombinedInput() {
            String[] args = {"-c=config.xml", "-p=/resources", "-s=login"};

            assertEquals("config.xml", Args.read(args, Args.Param.CONFIG_FILE).orElse(null));
            assertEquals("/resources", Args.read(args, Args.Param.PATH_TO_TEST_RESOURCES).orElse(null));
            assertEquals("login", Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS).orElse(null));
        }

        @Test
        void returnsEmptyForEmptyArgs() {
            String[] args = {};

            assertFalse(Args.read(args, Args.Param.CONFIG_FILE).isPresent());
            assertFalse(Args.read(args, Args.Param.PATH_TO_TEST_RESOURCES).isPresent());
            assertFalse(Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS).isPresent());
        }
    }

    @Nested
    class ParamEnum {

        @Test
        void configFileHasCorrectRegexp() {
            assertNotNull(Args.Param.CONFIG_FILE.getRegexp());
            assertTrue(Args.Param.CONFIG_FILE.getRegexp().contains("-c"));
            assertTrue(Args.Param.CONFIG_FILE.getRegexp().contains("--config"));
        }

        @Test
        void pathHasCorrectRegexp() {
            assertNotNull(Args.Param.PATH_TO_TEST_RESOURCES.getRegexp());
            assertTrue(Args.Param.PATH_TO_TEST_RESOURCES.getRegexp().contains("-p"));
            assertTrue(Args.Param.PATH_TO_TEST_RESOURCES.getRegexp().contains("--path"));
        }

        @Test
        void hasThreeValues() {
            assertEquals(3, Args.Param.values().length);
        }
    }
}
