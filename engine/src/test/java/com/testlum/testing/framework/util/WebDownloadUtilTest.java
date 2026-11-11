package com.testlum.testing.framework.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void parallelBrowsersDownloadSameFileWithoutOverwriting() throws Exception {
        int threads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<Path> allTransferred = Collections.synchronizedSet(new HashSet<>());
        String[] browsers = {"chrome", "edge", "firefox"};

        for (int i = 0; i < threads; i++) {
            String browser = browsers[i];
            executor.submit(() -> simulateParallelDownload(
                    browser, startLatch, doneLatch, allTransferred));
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertTrue(exists("sampleFile.jpeg"));
        assertTrue(exists("sampleFile (1).jpeg"));
        assertTrue(exists("sampleFile (2).jpeg"));
        assertEquals(threads, allTransferred.size());
    }

    private void simulateParallelDownload(final String browser,
                                         final CountDownLatch startLatch,
                                         final CountDownLatch doneLatch,
                                         final Set<Path> allTransferred) {
        try {
            Path downloadDir = webDownloadUtil.createDownloadDir(tempDir, browser);
            Path file = downloadDir.resolve("sampleFile.jpeg");
            Files.writeString(file, "content from " + browser);
            Set<Path> localTransferred = new HashSet<>();
            startLatch.await();
            webDownloadUtil.waitForDownloadIfInitiated(
                    downloadDir, tempDir, Collections.emptySet(), localTransferred);
            allTransferred.addAll(localTransferred);
            webDownloadUtil.deleteDownloadDir(downloadDir, tempDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            doneLatch.countDown();
        }
    }

    @Test
    void createDownloadDirCreatesUniqueSubdirectories() {
        Path chrome = webDownloadUtil.createDownloadDir(tempDir, "chrome");
        Path edge = webDownloadUtil.createDownloadDir(tempDir, "edge");

        assertNotNull(chrome);
        assertNotNull(edge);
        assertNotEquals(chrome, edge);
        assertTrue(Files.exists(chrome));
        assertTrue(Files.exists(edge));

        webDownloadUtil.deleteDownloadDir(chrome, tempDir);
        assertFalse(Files.exists(chrome));
        assertTrue(Files.exists(edge));
        webDownloadUtil.deleteDownloadDir(edge, tempDir);
        assertFalse(Files.exists(edge));
    }

    @Test
    void deleteDownloadDirDoesNotDeleteScenarioDir() {
        webDownloadUtil.deleteDownloadDir(tempDir, tempDir);
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void cleanupDownloadedFilesWithDownloadedFilesSetDeletesOnlyThoseFiles() throws IOException {
        create("pre_existing.txt");
        create("file1.pdf");
        create("file2.pdf");

        Set<Path> downloaded = Set.of(tempDir.resolve("file1.pdf"));
        webDownloadUtil.cleanupDownloadedFiles(tempDir, Set.of("pre_existing.txt"), false, downloaded);

        assertTrue(exists("pre_existing.txt"));
        assertFalse(exists("file1.pdf"));
        assertTrue(exists("file2.pdf"));
    }

    @Test
    void tenParallelBrowsersDownloadSameFileWithoutOverwriting() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<Path> allTransferred = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threads; i++) {
            final int id = i;
            executor.submit(() -> downloadConcurrently(
                    "browser_" + id, "data.csv", "content from " + id,
                    startLatch, doneLatch, allTransferred));
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(15, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads, allTransferred.size());
        assertTrue(exists("data.csv"));
        for (int i = 1; i < threads; i++) {
            assertTrue(exists("data (" + i + ").csv"), "Expected data (" + i + ").csv to exist");
        }
    }

    @Test
    void duplicateNamingConsistencyForFilesWithoutExtension() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<Path> allTransferred = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threads; i++) {
            final int id = i;
            executor.submit(() -> downloadConcurrently(
                    "browser_" + id, "document", "document payload " + id,
                    startLatch, doneLatch, allTransferred));
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads, allTransferred.size());
        assertTrue(exists("document"));
        for (int i = 1; i < threads; i++) {
            assertTrue(exists("document (" + i + ")"), "Expected document (" + i + ") to exist");
        }
    }

    @Test
    void duplicateNamingConsistencyForFilesWithMultipleDots() throws Exception {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<Path> allTransferred = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threads; i++) {
            final int id = i;
            executor.submit(() -> downloadConcurrently(
                    "browser_" + id, "archive.tar.gz", "tar payload " + id,
                    startLatch, doneLatch, allTransferred));
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads, allTransferred.size());
        assertTrue(exists("archive.tar.gz"));
        for (int i = 1; i < threads; i++) {
            assertTrue(exists("archive.tar (" + i + ").gz"),
                    "Expected archive.tar (" + i + ").gz to exist");
        }
    }

    @Test
    void preExistingFilesAreNeverOverwrittenOrCorrupted() throws Exception {
        create("sample.txt");
        Files.writeString(tempDir.resolve("sample.txt"), "ORIGINAL_BASE");
        create("sample (1).txt");
        Files.writeString(tempDir.resolve("sample (1).txt"), "ORIGINAL_1");
        create("sample (2).txt");
        Files.writeString(tempDir.resolve("sample (2).txt"), "ORIGINAL_2");

        int threads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        Set<Path> allTransferred = Collections.synchronizedSet(new HashSet<>());

        for (int i = 0; i < threads; i++) {
            final int id = i;
            executor.submit(() -> downloadConcurrently(
                    "browser_" + id, "sample.txt", "NEW_DOWNLOAD_" + id,
                    startLatch, doneLatch, allTransferred));
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(3, allTransferred.size());
        assertEquals("ORIGINAL_BASE", Files.readString(tempDir.resolve("sample.txt")));
        assertEquals("ORIGINAL_1", Files.readString(tempDir.resolve("sample (1).txt")));
        assertEquals("ORIGINAL_2", Files.readString(tempDir.resolve("sample (2).txt")));

        assertTrue(exists("sample (3).txt"));
        assertTrue(exists("sample (4).txt"));
        assertTrue(exists("sample (5).txt"));
    }

    private void downloadConcurrently(final String browser,
                                      final String fileName,
                                      final String content,
                                      final CountDownLatch startLatch,
                                      final CountDownLatch doneLatch,
                                      final Set<Path> allTransferred) {
        try {
            Path downloadDir = webDownloadUtil.createDownloadDir(tempDir, browser);
            Path file = downloadDir.resolve(fileName);
            Files.writeString(file, content);
            Set<Path> localTransferred = new HashSet<>();
            startLatch.await();
            webDownloadUtil.waitForDownloadIfInitiated(
                    downloadDir, tempDir, Collections.emptySet(), localTransferred);
            allTransferred.addAll(localTransferred);
            webDownloadUtil.deleteDownloadDir(downloadDir, tempDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            doneLatch.countDown();
        }
    }

    @Test
    void keepDownloadedFilesTrueVsFalseConcurrentCleanup() throws Exception {
        create("scenario.xml");
        final Set<String> preExisting = webDownloadUtil.snapshotFileNames(tempDir);

        Path dirKeep = webDownloadUtil.createDownloadDir(tempDir, "chrome_keep");
        Path fileKeep = dirKeep.resolve("report.pdf");
        Files.writeString(fileKeep, "content keep");
        Set<Path> downloadedKeep = new HashSet<>();
        webDownloadUtil.waitForDownloadIfInitiated(dirKeep, tempDir, Collections.emptySet(), downloadedKeep);
        webDownloadUtil.deleteDownloadDir(dirKeep, tempDir);

        Path dirDelete = webDownloadUtil.createDownloadDir(tempDir, "firefox_delete");
        Path fileDelete = dirDelete.resolve("report.pdf");
        Files.writeString(fileDelete, "content delete");
        Set<Path> downloadedDelete = new HashSet<>();
        webDownloadUtil.waitForDownloadIfInitiated(dirDelete, tempDir, Collections.emptySet(), downloadedDelete);
        webDownloadUtil.deleteDownloadDir(dirDelete, tempDir);

        assertEquals(1, downloadedKeep.size());
        assertEquals(1, downloadedDelete.size());
        Path keptFile = downloadedKeep.iterator().next();
        Path deletedFile = downloadedDelete.iterator().next();
        assertTrue(Files.exists(keptFile));
        assertTrue(Files.exists(deletedFile));

        webDownloadUtil.cleanupDownloadedFiles(tempDir, preExisting, true, downloadedKeep);
        assertTrue(Files.exists(keptFile));

        webDownloadUtil.cleanupDownloadedFiles(tempDir, preExisting, false, downloadedDelete);
        assertTrue(Files.exists(keptFile));
        assertFalse(Files.exists(deletedFile));
        assertTrue(exists("scenario.xml"));
    }

    @Test
    void safariCleanupDoesNotDeleteConcurrentChromiumDownloads() throws Exception {
        create("scenario.xml");
        final Set<String> preExisting = webDownloadUtil.snapshotFileNames(tempDir);

        create("chrome_kept_file.pdf");

        Path safariDownloads = tempDir.resolve("safari_downloads");
        Files.createDirectories(safariDownloads);
        Path safariFile = safariDownloads.resolve("safari_doc.pdf");
        Files.writeString(safariFile, "safari data");
        long startTime = System.currentTimeMillis() - 5000;

        Set<Path> safariDownloadedFiles = new HashSet<>();
        webDownloadUtil.processSafariDownloads(safariDownloads, tempDir, startTime, safariDownloadedFiles);
        assertTrue(exists("safari_doc.pdf"));
        assertEquals(1, safariDownloadedFiles.size());

        webDownloadUtil.cleanupDownloadedFiles(tempDir, preExisting, false, safariDownloadedFiles);

        assertTrue(exists("chrome_kept_file.pdf"),
                "Safari cleanup must NOT delete concurrent Chrome download!");
        assertFalse(exists("safari_doc.pdf"),
                "Safari downloaded file should be deleted when keepDownloadedFiles=false");
        assertTrue(exists("scenario.xml"));
    }

    @Test
    void concurrentScenarioWithZeroDownloadsDoesNotDeleteOtherScenarioDownloads() throws IOException {
        create("scenario.xml");
        final Set<String> preExisting = webDownloadUtil.snapshotFileNames(tempDir);

        create("concurrently_downloaded_file.pdf");

        Set<Path> zeroDownloadedFiles = Collections.emptySet();
        webDownloadUtil.cleanupDownloadedFiles(tempDir, preExisting, false, zeroDownloadedFiles);

        assertTrue(exists("scenario.xml"));
        assertTrue(exists("concurrently_downloaded_file.pdf"),
                "A concurrent scenario with 0 downloads must NOT delete files downloaded by other threads!");
    }

    @Test
    void safariDownloadsCombinedWithParallelChromiumDownloads() throws Exception {
        Path safariDownloads = tempDir.resolve("system_downloads");
        Files.createDirectories(safariDownloads);
        Path safariFile = safariDownloads.resolve("asset.png");
        Files.writeString(safariFile, "content from safari");

        Path chromeDir = webDownloadUtil.createDownloadDir(tempDir, "chrome");
        Path chromeFile = chromeDir.resolve("asset.png");
        Files.writeString(chromeFile, "content from chrome");

        Set<Path> chromeTransferred = new HashSet<>();
        long startTime = System.currentTimeMillis() - 5000;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                startLatch.await();
                webDownloadUtil.waitForDownloadIfInitiated(
                        chromeDir, tempDir, Collections.emptySet(), chromeTransferred);
                webDownloadUtil.deleteDownloadDir(chromeDir, tempDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                webDownloadUtil.processSafariDownloads(safariDownloads, tempDir, startTime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertTrue(exists("asset.png"));
        assertTrue(exists("asset (1).png"));
        assertFalse(Files.exists(safariFile));
    }

    @Test
    void errorHandlingWhenDownloadDirContainsLockedFilesOrIOError() throws IOException {
        Path downloadDir = webDownloadUtil.createDownloadDir(tempDir, "locked_browser");
        Path file = downloadDir.resolve("downloading.part");
        Files.writeString(file, "partial data");

        webDownloadUtil.deleteDownloadDir(downloadDir, tempDir);
        assertFalse(Files.exists(downloadDir));

        webDownloadUtil.deleteDownloadDir(null, tempDir);
        webDownloadUtil.deleteDownloadDir(tempDir.resolve("non_existent_dir"), tempDir);
        webDownloadUtil.cleanupDownloadedFiles(null, Collections.emptySet(), false);
        webDownloadUtil.cleanupDownloadedFiles(tempDir.resolve("non_existent_dir"), Collections.emptySet(), false);
    }

    private void create(final String name) throws IOException {
        Files.writeString(tempDir.resolve(name), "content");
    }

    private boolean exists(final String name) {
        return Files.exists(tempDir.resolve(name));
    }
}
