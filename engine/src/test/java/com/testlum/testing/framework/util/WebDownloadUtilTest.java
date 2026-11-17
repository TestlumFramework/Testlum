package com.testlum.testing.framework.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebDownloadUtilTest {

    @TempDir
    Path tempDir;

    private final WebDownloadUtil webDownloadUtil = new WebDownloadUtil();

    @Test
    void removesDownloadedFilesButKeepsGeneratedArtifacts() throws IOException {
        create("scenario.xml");
        final Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("downloaded.pdf");
        create("patch_2.xml");
        create("action_1_actual.json");
        create("actual_image_compared_to_expected.png");
        create("included_login_patch_2.xml");
        create("included_login_action_1_actual.json");
        create("included_login_actual_image_compared_to_expected.png");

        webDownloadUtil.cleanupDownloadedFiles(tempDir, snapshot, false);

        assertTrue(exists("scenario.xml"));
        assertTrue(exists("patch_2.xml"));
        assertTrue(exists("action_1_actual.json"));
        assertTrue(exists("actual_image_compared_to_expected.png"));
        assertTrue(exists("included_login_patch_2.xml"));
        assertTrue(exists("included_login_action_1_actual.json"));
        assertTrue(exists("included_login_actual_image_compared_to_expected.png"));
        assertFalse(exists("downloaded.pdf"));
    }

    @Test
    void keepsEverythingWhenDownloadedFilesAreKept() throws IOException {
        create("scenario.xml");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("downloaded.pdf");

        webDownloadUtil.cleanupDownloadedFiles(tempDir, snapshot, true);

        assertTrue(exists("downloaded.pdf"));
    }

    @Test
    void normalizesFirefoxDuplicateWhenOriginalExists() throws IOException {
        create("file.txt");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("file(1).txt");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("file.txt"));
        assertFalse(exists("file(1).txt"));
        assertTrue(exists("file (1).txt"));
    }

    @Test
    void normalizesSubsequentFirefoxDuplicatesSequentially() throws IOException {
        create("file.txt");
        create("file (1).txt");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("file(1).txt");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("file.txt"));
        assertTrue(exists("file (1).txt"));
        assertFalse(exists("file(1).txt"));
        assertTrue(exists("file (2).txt"));
    }

    @Test
    void normalizesFirefoxDuplicateWhenExistingDuplicateHasHigherIndex() throws IOException {
        create("file.txt");
        create("file (1).txt");
        create("file (2).txt");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("file(1).txt");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("file.txt"));
        assertTrue(exists("file (1).txt"));
        assertTrue(exists("file (2).txt"));
        assertFalse(exists("file(1).txt"));
        assertTrue(exists("file (3).txt"));
    }

    @Test
    void normalizesFirefoxDuplicateWithoutExtension() throws IOException {
        create("document");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("document(1)");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("document"));
        assertFalse(exists("document(1)"));
        assertTrue(exists("document (1)"));
    }

    @Test
    void doesNotNormalizeFileWhenOriginalDoesNotExist() throws IOException {
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("server_report(1).pdf");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("server_report(1).pdf"));
        assertFalse(exists("server_report (1).pdf"));
    }

    @Test
    void doesNotModifyChromeDuplicateFormat() throws IOException {
        create("file.txt");
        Set<String> snapshot = webDownloadUtil.snapshotFileNames(tempDir);
        create("file (1).txt");

        webDownloadUtil.normalizeDuplicateFileNames(tempDir, snapshot);

        assertTrue(exists("file.txt"));
        assertTrue(exists("file (1).txt"));
    }

    @Test
    void safariDownloadsResolvedToUniqueIndexWhenFileAlreadyExistsInScenarioDir() throws IOException {
        create("file.txt");
        Path safariDownloads = tempDir.resolve("downloads");
        Files.createDirectories(safariDownloads);
        Path downloadedFile = safariDownloads.resolve("file.txt");
        Files.writeString(downloadedFile, "new-content");

        long startTime = System.currentTimeMillis() - 5000;
        webDownloadUtil.processSafariDownloads(safariDownloads, tempDir, startTime);

        assertTrue(exists("file.txt"));
        assertTrue(exists("file (1).txt"));
        assertFalse(Files.exists(downloadedFile));
    }

    @Test
    void safariDownloadsIncrementsIndexWhenPreviousDuplicateExists() throws IOException {
        create("file.txt");
        create("file (1).txt");
        Path safariDownloads = tempDir.resolve("downloads");
        Files.createDirectories(safariDownloads);
        Path downloadedFile = safariDownloads.resolve("file.txt");
        Files.writeString(downloadedFile, "third-content");

        long startTime = System.currentTimeMillis() - 5000;
        webDownloadUtil.processSafariDownloads(safariDownloads, tempDir, startTime);

        assertTrue(exists("file.txt"));
        assertTrue(exists("file (1).txt"));
        assertTrue(exists("file (2).txt"));
        assertFalse(Files.exists(downloadedFile));
    }

    private void create(final String name) throws IOException {
        Files.writeString(tempDir.resolve(name), "content");
    }

    private boolean exists(final String name) {
        return Files.exists(tempDir.resolve(name));
    }
}
