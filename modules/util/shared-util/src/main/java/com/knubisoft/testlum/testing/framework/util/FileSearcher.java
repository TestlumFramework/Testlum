package com.knubisoft.testlum.testing.framework.util;

import com.google.common.base.Preconditions;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.FileLinkingException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@UtilityClass
public final class FileSearcher {

    private static final String ANSI_RESET = "\u001b[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String SLASH_SEPARATOR = "/";
    private static final String ENV_FOLDER_NOT_EXIST = "[%s] folder does not exist";
    private static final String DUPLICATE_FILENAME = ANSI_RED + "The [%s] folder and its subfolders contain "
            + "files with duplicate filenames - [%s]. Each file should have a unique name" + ANSI_RESET;
    private static final String DUPLICATE_FOLDER_NAME = ANSI_RED + "The [%s] folder and its subfolders contain "
            + "folders with duplicate names - [%s]. Each folder should have a unique name" + ANSI_RESET;
    private static final File TEST_RESOURCES_FOLDER = TestResourceSettings.getInstance().getTestResourcesFolder();
    private static final File DATA_FOLDER = TestResourceSettings.getInstance().getDataFolder();
    private static final File ENV_FOLDER = TestResourceSettings.getInstance().getEnvConfigFolder();
    private static final Map<String, File> DATA_FOLDER_FILES = collectFilesFromFolder(DATA_FOLDER);
    private static final Map<String, Map<String, File>> ENV_FOLDERS_FILES = collectFolderFilesFromFolder(ENV_FOLDER);

    public File searchFileFromDir(final File fromDir, final String name) {
        final String targetName = name.startsWith(SLASH_SEPARATOR) ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        return find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, TEST_RESOURCES_FOLDER, name));
    }

    private Optional<File> find(final File fromDir, final FilenameFilter filter) {
        if (TEST_RESOURCES_FOLDER.equals(fromDir)) {
            return Optional.empty();
        }
        File[] files = fromDir.listFiles(filter);
        if (nonNull(files) && files.length == 1) {
            return Optional.of(files[0]);
        }
        return find(fromDir.getParentFile(), filter);
    }

    @SneakyThrows
    public String searchFileToString(final String name, final File fromDir) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(searchFileFromDir(fromDir, name), StandardCharsets.UTF_8);
    }

    public File searchFileFromDataFolder(final String fileName) {
        final String targetName = fileName.startsWith(SLASH_SEPARATOR)
                ? fileName.substring(1) : fileName;
        File file = DATA_FOLDER_FILES.get(targetName);
        if (isNull(file)) {
            throw new FileLinkingException(DATA_FOLDER, TEST_RESOURCES_FOLDER, targetName);
        }
        return file;
    }

    public Optional<File> searchFileFromEnvFolder(final String folder, final String fileName) {
        Map<String, File> files = ENV_FOLDERS_FILES.get(folder);
        if (isNull(files)) {
            throw new FileLinkingException(String.format(ENV_FOLDER_NOT_EXIST, folder), ENV_FOLDER);
        }
        return Optional.ofNullable(files.get(fileName));
    }

    public Map<String, File> collectFilesFromFolder(final File folder) {
        return collectFilesFromFolder(folder, true);
    }

    private Map<String, File> collectFilesFromFolder(final File folder, final boolean recursive) {
        Map<String, File> files = new HashMap<>();
        FileUtils.listFiles(folder, null, recursive)
                .forEach(file -> {
                    files.computeIfPresent(file.getName(), (key, value) -> {
                        throw new DefaultFrameworkException(DUPLICATE_FILENAME, folder.getName(), file.getName());
                    });
                    files.put(file.getName(), file);
                });
        return Collections.unmodifiableMap(files);
    }

    public Map<String, Map<String, File>> collectFolderFilesFromFolder(final File root) {
        Map<String, Map<String, File>> folderFiles = new HashMap<>();
        FileUtils.listFilesAndDirs(root,
                        FileFilterUtils.falseFileFilter(), FileFilterUtils.directoryFileFilter())
                .forEach(folder -> {
                    folderFiles.computeIfPresent(folder.getName(), (key, value) -> {
                        throw new DefaultFrameworkException(DUPLICATE_FOLDER_NAME, root.getName(), folder.getName());
                    });
                    folderFiles.put(folder.getName(), collectFilesFromFolder(folder, false));
                });
        return folderFiles;
    }
}
