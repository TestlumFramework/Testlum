package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VISITING_PATH_LOG;
import static java.util.Objects.nonNull;

@Slf4j
@UtilityClass
public final class FileRemover {

    private static final FileVisitor FILE_VISITOR = new FileVisitor();

    public void clearActualFiles(final File folder) throws IOException {
        Files.walkFileTree(folder.toPath(), FILE_VISITOR);
    }

    private static class FileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(final Path file,
                                         final BasicFileAttributes attr) throws IOException {
            if (attr.isRegularFile() && isActualFile(file) || isScreenshotFile(file) || isActualImageFile(file)) {
                Files.delete(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir,
                                                  final IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file,
                                               final IOException exception) {
            log.error(FAILED_VISITING_PATH_LOG, file, exception);
            return FileVisitResult.CONTINUE;
        }

        private boolean isActualFile(final Path file) {
            return isFileWithSuffix(file, TestResourceSettings.ACTUAL_FILENAME);
        }

        private boolean isScreenshotFile(final Path file) {
            return isFileWithSuffix(file, TestResourceSettings.SCREENSHOT_FILENAME);
        }

        private boolean isActualImageFile(final Path file) {
            return isFileWithPrefix(file, TestResourceSettings.ACTUAL_IMAGE_PREFIX);
        }

        private boolean isFileWithSuffix(final Path file, final String suffix) {
            Path path = file.getFileName();
            return nonNull(path) && path.toString().endsWith(suffix);
        }

        private boolean isFileWithPrefix(final Path file, final String prefix) {
            Path path = file.getFileName();
            return nonNull(path) && path.toString().startsWith(prefix);
        }
    }
}
