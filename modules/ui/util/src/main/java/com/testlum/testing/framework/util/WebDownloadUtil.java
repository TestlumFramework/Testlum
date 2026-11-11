package com.testlum.testing.framework.util;

import com.testlum.testing.framework.TestResourceSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class WebDownloadUtil {

    private static final long DOWNLOAD_TIMEOUT_SECONDS = 60;
    private static final long POLLING_INTERVAL_MS = 300;
    private static final long DOWNLOAD_DETECTION_WINDOW_MS = 2000;
    private static final long STABILITY_THRESHOLD_MS = 2500;
    private static final String FINAL_ACTUAL_FILENAME = "action_";
    private static final String ACTUAL_IMAGE_PREFIX = "actual_image_compared_to_";
    private static final Pattern FIREFOX_DUPLICATE_PATTERN =
            Pattern.compile("^(.+?\\S)\\((\\d+)\\)(\\.[^.]+)?$");
    private static final Pattern EXISTING_INDEX_PATTERN =
            Pattern.compile("^(.+?)\\s?\\((\\d+)\\)(\\.[^.]+)?$");
    private static final int BASE_NAME_GROUP = 1;
    private static final int DUPLICATE_INDEX_GROUP = 2;
    private static final int EXTENSION_GROUP = 3;
    private static final int INITIAL_DUPLICATE_INDEX = 1;
    private static final int UUID_PREFIX_LENGTH = 8;

    private final ConcurrentMap<Path, Object> scenarioLocks = new ConcurrentHashMap<>();

    public Object getScenarioLock(final Path scenarioDir) {
        if (scenarioDir == null) {
            return new Object();
        }
        return scenarioLocks.computeIfAbsent(scenarioDir.toAbsolutePath().normalize(), k -> new Object());
    }

    public Path createDownloadDir(final Path scenarioDir, final String browserAlias) {
        if (scenarioDir == null) {
            return null;
        }
        String prefix = ".downloads_" + (browserAlias != null ? browserAlias : "browser")
                + "_" + System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, UUID_PREFIX_LENGTH);
        Path downloadDir = scenarioDir.resolve(prefix);
        try {
            return Files.createDirectories(downloadDir);
        } catch (IOException exception) {
            log.warn("Failed to create download dir {}, falling back to scenarioDir", downloadDir, exception);
            return scenarioDir;
        }
    }

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
        waitForDownloadIfInitiated(directory, directory, filesBeforeAction, null);
    }

    public void waitForDownloadIfInitiated(final Path downloadDir,
                                          final Path scenarioDir,
                                          final Set<String> filesBeforeAction,
                                          final Set<Path> downloadedFiles) {
        if (downloadDir == null || scenarioDir == null || !Files.exists(downloadDir)) {
            return;
        }
        if (isDownloadDetected(downloadDir, filesBeforeAction)) {
            waitForActiveDownloads(downloadDir);
            processCompletedDownloads(downloadDir, scenarioDir, filesBeforeAction, downloadedFiles);
        }
    }

    private void processCompletedDownloads(final Path downloadDir,
                                           final Path scenarioDir,
                                           final Set<String> filesBeforeAction,
                                           final Set<Path> downloadedFiles) {
        if (downloadDir.equals(scenarioDir)) {
            normalizeDuplicateFileNames(scenarioDir, filesBeforeAction);
            trackDirectDownloads(scenarioDir, filesBeforeAction, downloadedFiles);
        } else {
            normalizeDuplicateFileNames(downloadDir, filesBeforeAction);
            transferDownloadedFiles(downloadDir, scenarioDir, downloadedFiles);
        }
    }

    private void trackDirectDownloads(final Path scenarioDir,
                                      final Set<String> filesBeforeAction,
                                      final Set<Path> downloadedFiles) {
        if (downloadedFiles == null) {
            return;
        }
        for (String name : collectFileNames(scenarioDir)) {
            if (!filesBeforeAction.contains(name) && !isTestArtifact(name)) {
                downloadedFiles.add(scenarioDir.resolve(name));
            }
        }
    }

    private void transferDownloadedFiles(final Path downloadDir,
                                         final Path scenarioDir,
                                         final Set<Path> downloadedFiles) {
        synchronized (getScenarioLock(scenarioDir)) {
            try (Stream<Path> stream = Files.list(downloadDir)) {
                List<Path> files = stream.filter(Files::isRegularFile)
                        .filter(path -> !isTempDownloadFile(path))
                        .toList();
                for (Path source : files) {
                    moveDownloadedFile(source, scenarioDir, downloadedFiles);
                }
            } catch (IOException exception) {
                log.error("Failed to transfer files from {} to {}", downloadDir, scenarioDir, exception);
            }
        }
    }

    private void moveDownloadedFile(final Path source,
                                    final Path targetDirectory,
                                    final Set<Path> downloadedFiles) {
        try {
            Path targetPath = resolveTargetTransferPath(source, targetDirectory);
            Files.move(source, targetPath);
            trackDownloadedFile(downloadedFiles, targetPath);
            log.info("Transferred downloaded file to scenario: {} -> {}",
                    source.getFileName(), targetPath.getFileName());
        } catch (IOException exception) {
            log.error("Failed to move file to scenario directory: {}", source, exception);
        }
    }

    private Path resolveTargetTransferPath(final Path source, final Path targetDirectory) {
        String fileName = source.getFileName().toString();
        Path targetPath = targetDirectory.resolve(fileName);
        if (Files.exists(targetPath)) {
            return resolveUniquePathForExistingFile(targetDirectory, fileName);
        }
        return targetPath;
    }

    private void trackDownloadedFile(final Set<Path> downloadedFiles, final Path targetPath) {
        if (downloadedFiles != null) {
            downloadedFiles.add(targetPath);
        }
    }

    void normalizeDuplicateFileNames(final Path directory, final Set<String> filesBeforeAction) {
        Set<String> currentFiles = collectFileNames(directory);
        for (String fileName : currentFiles) {
            if (!filesBeforeAction.contains(fileName) && !isTestArtifact(fileName)) {
                normalizeIfDuplicate(directory, fileName, filesBeforeAction);
            }
        }
    }

    private void normalizeIfDuplicate(final Path directory,
                                      final String fileName,
                                      final Set<String> filesBeforeAction) {
        Matcher matcher = FIREFOX_DUPLICATE_PATTERN.matcher(fileName);
        if (!matcher.matches()) {
            return;
        }
        String base = matcher.group(BASE_NAME_GROUP);
        int index = Integer.parseInt(matcher.group(DUPLICATE_INDEX_GROUP));
        String ext = matcher.group(EXTENSION_GROUP) != null ? matcher.group(EXTENSION_GROUP) : "";
        if (!isDuplicateOfExistingFile(directory, base, ext, filesBeforeAction)) {
            return;
        }
        renameDuplicateFile(directory, fileName, base, index, ext);
    }

    private boolean isDuplicateOfExistingFile(final Path directory,
                                              final String base,
                                              final String ext,
                                              final Set<String> filesBeforeAction) {
        String originalName = base + ext;
        return Files.exists(directory.resolve(originalName)) || filesBeforeAction.contains(originalName);
    }

    private void renameDuplicateFile(final Path directory,
                                     final String fileName,
                                     final String base,
                                     final int index,
                                     final String ext) {
        Path sourcePath = directory.resolve(fileName);
        Path targetPath = resolveUniqueDuplicatePath(directory, base, index, ext);
        try {
            Files.move(sourcePath, targetPath);
            log.info("Normalized duplicate downloaded file name: {} -> {}", fileName, targetPath.getFileName());
        } catch (IOException exception) {
            log.error("Failed to rename duplicate downloaded file: {}", fileName, exception);
        }
    }

    private Path resolveUniqueDuplicatePath(final Path directory,
                                            final String base,
                                            final int startIndex,
                                            final String ext) {
        int index = Math.max(INITIAL_DUPLICATE_INDEX, startIndex);
        Path targetPath = directory.resolve(formatDuplicateFileName(base, index, ext));
        while (Files.exists(targetPath)) {
            index++;
            targetPath = directory.resolve(formatDuplicateFileName(base, index, ext));
        }
        return targetPath;
    }

    private String formatDuplicateFileName(final String base, final int index, final String ext) {
        return base + " (" + index + ")" + ext;
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
               && System.currentTimeMillis() - stableSinceTimestamp > STABILITY_THRESHOLD_MS;
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
        moveSystemDownloadsToScenarioDir(scenarioDirectory, startTimeEpochMs, null);
    }

    public void moveSystemDownloadsToScenarioDir(final Path scenarioDirectory,
                                                final long startTimeEpochMs,
                                                final Set<Path> downloadedFiles) {
        if (scenarioDirectory == null) {
            return;
        }
        Path systemDownloadsDirectory = Paths.get(System.getProperty("user.home"), "Downloads");
        if (!Files.isDirectory(systemDownloadsDirectory)) {
            return;
        }
        processSafariDownloads(systemDownloadsDirectory, scenarioDirectory, startTimeEpochMs, downloadedFiles);
    }

    void processSafariDownloads(final Path sourceDirectory, final Path targetDirectory, final long startTime) {
        processSafariDownloads(sourceDirectory, targetDirectory, startTime, null);
    }

    void processSafariDownloads(final Path sourceDirectory,
                                final Path targetDirectory,
                                final long startTime,
                                final Set<Path> downloadedFiles) {
        synchronized (getScenarioLock(targetDirectory)) {
            try (Stream<Path> stream = Files.list(sourceDirectory)) {
                stream.filter(Files::isRegularFile)
                        .filter(path -> isEligibleSafariFile(path, startTime))
                        .forEach(path -> moveDownloadedFile(path, targetDirectory, downloadedFiles));
            } catch (IOException exception) {
                log.warn("Failed to scan system Downloads folder", exception);
            }
        }
    }

    private boolean isEligibleSafariFile(final Path path, final long startTime) {
        return !isTempDownloadFile(path) && getFileLastModifiedTime(path) > startTime;
    }

    private Path resolveUniquePathForExistingFile(final Path directory, final String fileName) {
        Matcher matcher = EXISTING_INDEX_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            String base = matcher.group(BASE_NAME_GROUP);
            int nextIndex = Integer.parseInt(matcher.group(DUPLICATE_INDEX_GROUP)) + 1;
            String ext = matcher.group(EXTENSION_GROUP) != null ? matcher.group(EXTENSION_GROUP) : "";
            return resolveUniqueDuplicatePath(directory, base, nextIndex, ext);
        }
        return resolveUniquePathFromUnindexedFile(directory, fileName);
    }

    private Path resolveUniquePathFromUnindexedFile(final Path directory, final String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        String base = (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
        String ext = (dotIndex > 0) ? fileName.substring(dotIndex) : "";
        return resolveUniqueDuplicatePath(directory, base, INITIAL_DUPLICATE_INDEX, ext);
    }

    public void cleanupDownloadedFiles(final Path directory,
                                      final Set<String> existingFiles,
                                      final boolean keepFiles) {
        cleanupDownloadedFiles(directory, existingFiles, keepFiles, null);
    }

    public void cleanupDownloadedFiles(final Path directory,
                                      final Set<String> existingFiles,
                                      final boolean keepFiles,
                                      final Set<Path> downloadedFiles) {
        if (keepFiles || directory == null || !Files.exists(directory)) {
            return;
        }
        if (downloadedFiles != null) {
            downloadedFiles.forEach(this::deleteFile);
            return;
        }
        performCleanup(directory, existingFiles);
    }

    public void deleteDownloadDir(final Path downloadDir, final Path scenarioDir) {
        if (downloadDir == null || downloadDir.equals(scenarioDir) || !Files.exists(downloadDir)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(downloadDir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(this::deleteFileSilently);
        } catch (IOException exception) {
            log.warn("Failed to delete isolated download directory: {}", downloadDir, exception);
        }
    }

    private void deleteFileSilently(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.debug("Failed to delete file silently: {}", path, exception);
        }
    }

    private void performCleanup(final Path directory, final Set<String> existingFiles) {
        try (Stream<Path> stream = Files.list(directory)) {
            stream.filter(Files::isRegularFile)
                  .filter(path -> shouldFileBeDeleted(existingFiles, path))
                  .forEach(this::deleteFile);
        } catch (IOException exception) {
            log.warn("Failed to cleanup directory: {}", directory, exception);
        }
    }

    private boolean shouldFileBeDeleted(final Set<String> existingFiles, final Path path) {
        String fileName = path.getFileName().toString();
        if (isTestArtifact(fileName)) {
            return false;
        }
        return !existingFiles.contains(fileName);
    }

    private boolean isTestArtifact(final String fileName) {
        boolean isActual = fileName.startsWith(FINAL_ACTUAL_FILENAME)
                || fileName.startsWith(TestResourceSettings.INCLUDED_PREFIX)
                && fileName.endsWith(TestResourceSettings.ACTUAL_FILENAME);
        boolean isImage = fileName.startsWith(ACTUAL_IMAGE_PREFIX)
                || fileName.startsWith(TestResourceSettings.INCLUDED_PREFIX)
                && fileName.contains(ACTUAL_IMAGE_PREFIX);
        boolean isHealingPatch = fileName.startsWith(TestResourceSettings.PATCH_FILE_PREFIX)
                || fileName.startsWith(TestResourceSettings.INCLUDED_PREFIX)
                && fileName.contains(TestResourceSettings.PATCH_FILE_PREFIX);
        return isActual || isImage || isHealingPatch;
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