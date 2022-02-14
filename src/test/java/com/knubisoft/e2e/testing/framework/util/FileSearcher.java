package com.knubisoft.e2e.testing.framework.util;

import com.google.common.base.Preconditions;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.FileLinkingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Slf4j
public final class FileSearcher {

    private final File root;
    private final File fromDir;
    private final boolean overridePathToVolume;

    public FileSearcher(final File root, final boolean overridePathToVolume) {
        this(root, null, overridePathToVolume);
    }

    //CHECKSTYLE:OFF
    public File search(final File fromDir, final String name) {
        final String targetName = name.startsWith(DelimiterConstant.SLASH_SEPARATOR) ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        File result = find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, root, name));
        if (GlobalTestConfigurationProvider.provide().isDebugMode()) {
            log.info("RESULT OF FILE SEARCHING: {}", result);
            log.info("IS OVERRIDE PATH TO VOLUME: {}", overridePathToVolume);
        }
        if (result.exists() && isFileAnImage(result) && overridePathToVolume) {
            log.info("Because system is running inside docker switching files to mounted directory");
            final String search = "src/test/resources";
            int idx = result.getAbsolutePath().indexOf(search) + search.length();
            File mounted = new File("/opt/src/test/resources", result.getAbsolutePath().substring(idx));
            log.info("Original location: " + result.getAbsolutePath() + " new location " + mounted.getAbsolutePath());

            if (!result.exists() && !mounted.exists()) {
                throw new RuntimeException("File not found. Checked paths " +
                        result.getAbsolutePath() +
                        "  and  " +
                        mounted.getAbsolutePath());
            }
            result = mounted;
        }
        return result;
    }

    public File search(final String name) {
        Preconditions.checkNotNull(fromDir);
        return search(fromDir, name);
    }

    private boolean isFileAnImage(File result) {
        String mimetype = new MimetypesFileTypeMap().getContentType(result);
        String type = mimetype.split(DelimiterConstant.SLASH_SEPARATOR)[0];
        if (GlobalTestConfigurationProvider.provide().isDebugMode()) {
            log.info("TYPE OF FILE RESULT: {}", type);
        }
        if (type.equals("image")) {
            log.info("File {} is an image", result);
            return true;
        }
        return false;
    }

    //CHECKSTYLE:ON

    private Optional<File> find(final File fromDir, final FilenameFilter filter) {
        if (root.equals(fromDir)) {
            return Optional.empty();
        }
        File[] files = fromDir.listFiles(filter);
        if (files != null && files.length == 1) {
            return Optional.of(files[0]);
        }
        return find(fromDir.getParentFile(), filter);
    }

    @SneakyThrows
    public String searchFileAndReadToString(final String name) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(search(fromDir, name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String searchFileAndReadToString(final File fromDir, final String name) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(search(fromDir, name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String searchFileToString(final String name) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(search(fromDir, name), StandardCharsets.UTF_8);
    }
}
