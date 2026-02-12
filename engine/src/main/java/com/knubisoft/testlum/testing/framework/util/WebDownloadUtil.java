package com.knubisoft.testlum.testing.framework.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class WebDownloadUtil {

    private static final long DOWNLOAD_TIMEOUT_SECONDS = 60;
    private static final long POLLING_INTERVAL_MS = 300;
    private static final long DOWNLOAD_DETECTION_WINDOW_MS = 2000;
    private static final long STABILITY_THRESHOLD_MS = 2500;
    private static final String ACTUAL_FILENAME = "actual.json";
    private static final String FINAL_ACTUAL_FILENAME = "action_%s_" + ACTUAL_FILENAME;

    public Path resolveScenarioDir(final File scenarioFile) {
        if (scenarioFile == null) {
            return null;
        }
        File parentDirectory = scenarioFile.getParentFile();
        if (parentDirectory == null) {
            return null;
        }
        return parentDirectory.toPath().toAbsolutePath().normalize();
    }

    public Set<String> snapshotFileNames(final Path directory) {
        if (directory == null || !Files.isDirectory(directory)) {
            return Collections.emptySet();
        }
        return collectFileNames(directory);
    }

    private Set<String> collectFileNames(final Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (IOException exception) {
            return Collections.emptySet();
        }
    }

    public void waitForDownloadIfInitiated(final Path directory, final Set<String> filesBeforeAction) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        if (isDownloadDetected(directory, filesBeforeAction)) {
            waitForActiveDownloads(directory);
        }
    }

    private boolean isDownloadDetected(final Path directory, final Set<String> filesBeforeAction) {
        long deadline = System.currentTimeMillis() + DOWNLOAD_DETECTION_WINDOW_MS;
        while (System.currentTimeMillis() < deadline) {
            if (containsNewOrTempFiles(directory, filesBeforeAction)) {
                return true;
            }
            sleep();
        }
        return false;
    }

    private boolean containsNewOrTempFiles(final Path directory, final Set<String> filesBeforeAction) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile)
                    .anyMatch(path -> isNewOrTempFile(path, filesBeforeAction));
        } catch (IOException exception) {
            return false;
        }
    }

    private boolean isNewOrTempFile(final Path path, final Set<String> filesBeforeAction) {
        return isTempDownloadFile(path) || !filesBeforeAction.contains(path.getFileName().toString());
    }

    public void waitForActiveDownloads(final Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }
        performWaitLoop(directory);
    }

    private void performWaitLoop(final Path directory) {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(DOWNLOAD_TIMEOUT_SECONDS);
        long stableSinceTimestamp = 0;
        while (System.currentTimeMillis() < deadline) {
            stableSinceTimestamp = processStabilityCycle(directory, stableSinceTimestamp);
            if (isStabilityThresholdMet(stableSinceTimestamp)) {
                return;
            }
            sleep();
        }
    }

    private long processStabilityCycle(final Path directory, final long currentStableSince) {
        if (containsActiveDownloads(directory)) {
            return 0;
        }
        return (currentStableSince == 0) ? System.currentTimeMillis() : currentStableSince;
    }

    private boolean isStabilityThresholdMet(final long stableSinceTimestamp) {
        return stableSinceTimestamp > 0
                && (System.currentTimeMillis() - stableSinceTimestamp > STABILITY_THRESHOLD_MS);
    }

    public boolean isTempDownloadFile(final Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return isStandardTempExtension(fileName) || isChromeSpecificTempFile(fileName);
    }

    private boolean isStandardTempExtension(final String fileName) {
        return fileName.endsWith(".crdownload") || fileName.endsWith(".part")
                || fileName.endsWith(".tmp") || fileName.endsWith(".download");
    }

    private boolean isChromeSpecificTempFile(final String fileName) {
        return fileName.startsWith("unconfirmed") || fileName.startsWith(".com.google.chrome")
                || fileName.startsWith(".org.chromium.chromium");
    }

    private boolean containsActiveDownloads(final Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile).anyMatch(this::isTempDownloadFile);
        } catch (IOException exception) {
            return true;
        }
    }

    public void moveSystemDownloadsToScenarioDir(final Path scenarioDirectory, final long startTimeEpochMs) {
        if (scenarioDirectory == null) {
            return;
        }
        Path systemDownloadsDirectory = Paths.get(System.getProperty("user.home"), "Downloads");
        if (!Files.isDirectory(systemDownloadsDirectory)) {
            return;
        }
        processSafariDownloads(systemDownloadsDirectory, scenarioDirectory, startTimeEpochMs);
    }

    private void processSafariDownloads(final Path sourceDirectory, final Path targetDirectory, final long startTime) {
        try (Stream<Path> stream = Files.list(sourceDirectory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> isEligibleSafariFile(path, startTime))
                    .forEach(path -> moveFile(path, targetDirectory));
        } catch (IOException exception) {
            log.warn("Failed to scan system Downloads folder", exception);
        }
    }

    private boolean isEligibleSafariFile(final Path path, final long startTime) {
        return !isTempDownloadFile(path) && getFileLastModifiedTime(path) > startTime;
    }

    private void moveFile(final Path source, final Path targetDirectory) {
        try {
            Path targetPath = targetDirectory.resolve(source.getFileName());
            Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error("Failed to move file from Downloads: {}", source, exception);
        }
    }

    public void cleanupDownloadedFiles(final Path directory, final Set<String> existingFiles, final boolean keepFiles) {
        if (keepFiles || directory == null || !Files.exists(directory)) {
            return;
        }
        performCleanup(directory, existingFiles);
    }

    private void performCleanup(final Path directory, final Set<String> existingFiles) {
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> !existingFiles.contains(path.getFileName().toString())
                                    && path.getFileName().toString().startsWith(FINAL_ACTUAL_FILENAME))
                    .forEach(this::deleteFile);
        } catch (IOException exception) {
            log.warn("Failed to cleanup directory: {}", directory, exception);
        }
    }

    private void deleteFile(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("Failed to delete file: {}", path, exception);
        }
    }

    private long getFileLastModifiedTime(final Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException exception) {
            return 0L;
        }
    }

    private void sleep() {
        try {
            Thread.sleep(WebDownloadUtil.POLLING_INTERVAL_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}