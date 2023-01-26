package com.knubisoft.cott.testing.framework.util;

import com.google.common.base.Preconditions;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.FileLinkingException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DUPLICATE_FILENAME;

@UtilityClass
@Slf4j
public final class FileSearcher {
    private static final File TEST_RESOURCES_FOLDER = TestResourceSettings.getInstance().getTestResourcesFolder();
    private static final File DATA_FOLDER = TestResourceSettings.getInstance().getDataFolder();
    private static final File CONFIG_FOLDER = TestResourceSettings.getInstance().getConfigFolder();
    private static final List<File> ENV_FOLDERS = TestResourceSettings.getInstance().getEnvFolders();
    private static final Map<String, File> DATA_FOLDER_FILES = collectFilesFromFolder(DATA_FOLDER);
    private static final Map<String, Map<String, File>> ENV_FOLDERS_FILES = collectFilesFromEnvFolders();

    public File searchFileFromDir(final File fromDir, final String name) {
        final String targetName = name.startsWith(DelimiterConstant.SLASH_SEPARATOR)
                ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        return find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, TEST_RESOURCES_FOLDER, name));
    }

    @SneakyThrows
    public String searchFileToString(final String name, final File fromDir) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(searchFileFromDir(fromDir, name), StandardCharsets.UTF_8);
    }

    public File searchFileFromDataFolder(final String fileName) {
        final String targetName = fileName.startsWith(DelimiterConstant.SLASH_SEPARATOR)
                ? fileName.substring(1) : fileName;
        File file = DATA_FOLDER_FILES.get(targetName);
        if (Objects.isNull(file)) {
            throw new FileLinkingException(DATA_FOLDER, DATA_FOLDER, targetName);
        }
        return file;
    }

    public File searchFileFromEnvFolder(final String  folder, final String fileName) {
        File file = ENV_FOLDERS_FILES.get(folder).get(fileName);
        if (Objects.isNull(file)) {
            throw new DefaultFrameworkException("file not found in env folder");
        }
        return file;
    }

    public Map<String, Map<String, File>> collectFilesFromEnvFolders() {
        Map<String, Map<String, File>> envsFiles = new HashMap<>();
        ENV_FOLDERS.forEach(folder -> envsFiles.put(folder.getName(), collectFilesFromFolder(folder)));
        return envsFiles;
    }

    public Map<String, File> collectFilesFromFolder(final File folder) {
        Map<String, File> files = new HashMap<>();
        FileUtils.listFiles(folder, null, true)
                .forEach(file -> {
                    files.computeIfPresent(file.getName(), (key, value) -> {
                        throw new DefaultFrameworkException(DUPLICATE_FILENAME, folder.getName(), file.getName());
                    });
                    files.put(file.getName(), file);
                });
        return Collections.unmodifiableMap(files);
    }

    private Optional<File> find(final File fromDir, final FilenameFilter filter) {
        if (TEST_RESOURCES_FOLDER.equals(fromDir)) {
            return Optional.empty();
        }
        File[] files = fromDir.listFiles(filter);
        if (files != null && files.length == 1) {
            return Optional.of(files[0]);
        }
        return find(fromDir.getParentFile(), filter);
    }

}
