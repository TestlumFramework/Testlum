package com.knubisoft.testlum.testing.framework;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestResourceSettingsTest {

    @TempDir
    File tempDir;

    private void createRequiredFolders() throws IOException {
        new File(tempDir, "global.xml").createNewFile();
        new File(tempDir, "config").mkdirs();
        new File(tempDir, "scenarios").mkdirs();
        new File(tempDir, "data").mkdirs();
        new File(tempDir, "locators/pages").mkdirs();
        new File(tempDir, "locators/component").mkdirs();
    }

    @Nested
    class Init {
        @Test
        void initializesStaticFields() {
            TestResourceSettings.init("config.xml", "/some/path", Optional.empty());
            assertEquals("config.xml", TestResourceSettings.getConfigFileName());
            assertEquals("/some/path", TestResourceSettings.getPathToTestResources());
            assertTrue(TestResourceSettings.getScenarioScope().isEmpty());
        }

        @Test
        void initializesWithScenarioScope() {
            TestResourceSettings.init("config.xml", "/some/path", Optional.of("smoke"));
            assertEquals("smoke", TestResourceSettings.getScenarioScope().get());
        }
    }

    @Nested
    class Construction {
        @Test
        void createsSettingsWithValidPaths() throws IOException {
            createRequiredFolders();
            TestResourceSettings.init(
                    "global.xml", tempDir.getAbsolutePath(), Optional.empty());
            final TestResourceSettings settings = new TestResourceSettings();
            assertNotNull(settings.getTestResourcesFolder());
            assertNotNull(settings.getConfigFile());
            assertNotNull(settings.getEnvConfigFolder());
            assertNotNull(settings.getScenariosFolder());
            assertNotNull(settings.getDataFolder());
        }

        @Test
        void throwsWhenPathDoesNotExist() {
            TestResourceSettings.init("cfg.xml", "/nonexistent/path", Optional.empty());
            assertThrows(IllegalArgumentException.class, TestResourceSettings::new);
        }

        @Test
        void throwsWhenRequiredSubfolderMissing() throws IOException {
            new File(tempDir, "partial.xml").createNewFile();
            TestResourceSettings.init(
                    "partial.xml", tempDir.getAbsolutePath(), Optional.empty());
            assertThrows(IllegalArgumentException.class, TestResourceSettings::new);
        }

        @Test
        void handlesScenarioScopeFolder() throws IOException {
            createRequiredFolders();
            new File(tempDir, "scenarios/smoke").mkdirs();
            TestResourceSettings.init(
                    "global.xml", tempDir.getAbsolutePath(), Optional.of("smoke"));
            final TestResourceSettings settings = new TestResourceSettings();
            assertTrue(settings.getScenarioScopeFolder().isPresent());
            assertEquals("smoke", settings.getScenarioScopeFolder().get().getName());
        }
    }

    @Nested
    class Constants {
        @Test
        void scenarioFilenameIsCorrect() {
            assertEquals("scenario.xml", TestResourceSettings.SCENARIO_FILENAME);
        }

        @Test
        void actualFilenameIsCorrect() {
            assertEquals("actual.json", TestResourceSettings.ACTUAL_FILENAME);
        }

        @Test
        void screenshotFilenameIsCorrect() {
            assertEquals("screenshot.jpg", TestResourceSettings.SCREENSHOT_FILENAME);
        }
    }
}
