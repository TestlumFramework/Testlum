package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileRemoverTest {

    @TempDir
    Path tempDir;

    private final FileRemover fileRemover = new FileRemover();

    @Nested
    class ClearActualFiles {

        @Test
        void deletesActualFiles() throws IOException {
            File actual = Files.createFile(tempDir.resolve("test" + TestResourceSettings.ACTUAL_FILENAME)).toFile();
            assertTrue(actual.exists());
            fileRemover.clearActualFiles(tempDir.toFile());
            assertFalse(actual.exists());
        }

        @Test
        void deletesScreenshotFiles() throws IOException {
            File screenshot = Files.createFile(tempDir.resolve("test"
                    + TestResourceSettings.SCREENSHOT_FILENAME)).toFile();
            assertTrue(screenshot.exists());
            fileRemover.clearActualFiles(tempDir.toFile());
            assertFalse(screenshot.exists());
        }

        @Test
        void deletesActualImageFiles() throws IOException {
            File actualImage = Files.createFile(
                    tempDir.resolve(TestResourceSettings.ACTUAL_IMAGE_PREFIX + "test.png")).toFile();
            assertTrue(actualImage.exists());
            fileRemover.clearActualFiles(tempDir.toFile());
            assertFalse(actualImage.exists());
        }

        @Test
        void doesNotDeleteNonMatchingFiles() throws IOException {
            File regular = Files.createFile(tempDir.resolve("regular.txt")).toFile();
            assertTrue(regular.exists());
            fileRemover.clearActualFiles(tempDir.toFile());
            assertTrue(regular.exists());
        }

        @Test
        void handlesSubdirectories() throws IOException {
            Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
            File actual = Files.createFile(subDir.resolve("test" + TestResourceSettings.ACTUAL_FILENAME)).toFile();
            File regular = Files.createFile(subDir.resolve("keep.txt")).toFile();
            fileRemover.clearActualFiles(tempDir.toFile());
            assertFalse(actual.exists());
            assertTrue(regular.exists());
        }

        @Test
        void handlesEmptyDirectory() throws IOException {
            fileRemover.clearActualFiles(tempDir.toFile());
        }
    }
}
