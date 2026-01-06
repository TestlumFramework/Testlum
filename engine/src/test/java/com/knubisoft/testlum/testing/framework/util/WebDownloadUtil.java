package com.knubisoft.testlum.testing.framework.util;

import lombok.Getter;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WebDownloadUtil {

    private static final long TIMEOUT_MS = 60_000L;
    private static final long POLL_MS = 250L;
    private static final ThreadLocal<Set<Path>> DOWNLOADED_FILES = ThreadLocal.withInitial(HashSet::new);

    public static DownloadContext prepareDownload(final WebDriver driver, final File scenarioFile) {
        Path scenarioDir = resolveScenarioDir(scenarioFile);
        if (scenarioDir == null || !Files.isDirectory(scenarioDir)) {
            return DownloadContext.inactive();
        }

        configureChromiumDownloadDir(driver, scenarioDir);
        Set<String> before = snapshotFileNames(scenarioDir);

        return new DownloadContext(true, scenarioDir, before, System.currentTimeMillis());
    }

    public static void captureDownloadedFile(final DownloadContext ctx) {
        if (ctx == null || !ctx.isActive() || ctx.getScenarioDir() == null) {
            return;
        }

        Path dir = ctx.getScenarioDir();
        if (!Files.isDirectory(dir)) {
            return;
        }

        final long QUICK_DETECT_MS = 1500L;
        long quickDeadline = System.currentTimeMillis() + QUICK_DETECT_MS;

        boolean detected = false;

        while (System.currentTimeMillis() <= quickDeadline) {
            List<Path> newFiles = listNewFiles(dir, ctx.getBeforeFileNames());
            for (Path path : newFiles) {
                registerDownloadedFile(path);
            }
            if (!newFiles.isEmpty()) {
                detected = true;
                break;
            }
            sleep(POLL_MS);
        }

        if (!detected) {
            return;
        }

        long deadline = System.currentTimeMillis() + TIMEOUT_MS;

        while (System.currentTimeMillis() <= deadline) {
            List<Path> newFiles = listNewFiles(dir, ctx.getBeforeFileNames());

            Path ready = newestReadyFile(newFiles);
            if (ready != null) {
                long size = safeSize(ready);
                if (size > 0) {
                    return;
                }
            }

            sleep(POLL_MS);
        }

    }

    private static List<Path> listNewFiles(final Path dir, final Set<String> beforeFileNames) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> beforeFileNames == null || !beforeFileNames.contains(p.getFileName().toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private static Path newestReadyFile(final List<Path> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        return candidates.stream()
                .filter(path -> !isTempDownloadFile(path))
                .max(Comparator.comparingLong(WebDownloadUtil::safeLastModified))
                .orElse(null);
    }

    public static Path resolveScenarioDir(final File scenarioFile) {
        if (scenarioFile == null) {
            return null;
        }
        File parent = scenarioFile.getParentFile();
        return parent == null ? null : parent.toPath().toAbsolutePath().normalize();
    }

    public static void configureChromiumDownloadDir(final WebDriver driver, final Path dir) {
        if (driver == null || dir == null) {
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("behavior", "allow");
        params.put("downloadPath", dir.toString());

        if (tryExecuteCdp(driver, "Page.setDownloadBehavior", params)) {
            return;
        }

        tryExecuteCdp(driver, "Browser.setDownloadBehavior", params);
    }

    public static Set<String> snapshotFileNames(final Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            return Collections.emptySet();
        }
    }

    public static boolean isTempDownloadFile(final Path p) {
        String name = p.getFileName().toString().toLowerCase();
        return name.endsWith(".crdownload") || name.endsWith(".part") || name.endsWith(".tmp");
    }

    public static long safeLastModified(final Path p) {
        try {
            return Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    public static long safeSize(final Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            return 0L;
        }
    }

    public static void sleep(final long ms) {
        try {
            Thread.sleep(Math.max(0L, ms));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void registerDownloadedFile(final Path file) {
        if (file == null) {
            return;
        }
        try {
            DOWNLOADED_FILES.get().add(file.toAbsolutePath().normalize());
        } catch (Exception ignored) { }
    }

    public static void cleanupDownloadedFiles(final boolean keepFiles) {
        try {
            if (!keepFiles) {
                for (Path path : DOWNLOADED_FILES.get()) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) { }
                }
            }
        } finally {
            DOWNLOADED_FILES.remove();
        }
    }

    private static boolean tryExecuteCdp(final WebDriver driver,
                                         final String command,
                                         final Map<String, Object> params) {
        try {
            Method m = driver.getClass().getMethod("executeCdpCommand", String.class, Map.class);
            m.invoke(driver, command, params);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Getter
    public static final class DownloadContext {
        private final boolean active;
        private final Path scenarioDir;
        private final Set<String> beforeFileNames;
        private final long preparedAtMs;

        private DownloadContext(final boolean active,
                                final Path scenarioDir,
                                final Set<String> beforeFileNames,
                                final long preparedAtMs) {
            this.active = active;
            this.scenarioDir = scenarioDir;
            this.beforeFileNames = beforeFileNames;
            this.preparedAtMs = preparedAtMs;
        }

        public static DownloadContext inactive() {
            return new DownloadContext(false, null, Collections.emptySet(), 0L);
        }
    }
}