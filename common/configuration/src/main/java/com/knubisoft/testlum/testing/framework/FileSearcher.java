package com.knubisoft.testlum.testing.framework;

import com.google.common.base.Preconditions;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.exception.FileLinkingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public final class FileSearcher {

    private static final String SLASH_SEPARATOR = "/";
    private static final String ENV_FOLDER_NOT_EXIST = "[%s] folder does not exist";
    private static final String DUPLICATE_FILENAME = LogFormat.withRed(
            "The [%s] folder and its subfolders contain files with "
            + "duplicate filenames - [%s]. Each file should have a unique name");
    private static final String DUPLICATE_FOLDER_NAME = LogFormat.withRed(
            "The [%s] folder and its subfolders contain folders with "
            + "duplicate names - [%s]. Each folder should have a unique name");

    private final File testResourcesFolder;
    private final File dataFolder;
    private final File envFolder;
    private final Map<String, File> dataFolderFiles;
    private final Map<String, Map<String, File>> envFoldersFiles;
    private final TestResourceSettings testResourceSettings;

    public FileSearcher(final TestResourceSettings testResourceSettings) {
        this.testResourceSettings = testResourceSettings;
        this.testResourcesFolder = this.testResourceSettings.getTestResourcesFolder();
        this.dataFolder = this.testResourceSettings.getDataFolder();
        this.envFolder = this.testResourceSettings.getEnvConfigFolder();
        this.dataFolderFiles = collectFilesFromFolder(dataFolder);
        this.envFoldersFiles = collectFolderFilesFromFolder(envFolder);
    }

    public File searchFileFromDir(final File fromDir, final String name) {
        final String targetName = name.startsWith(SLASH_SEPARATOR) ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        return find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, testResourcesFolder, name));
    }

    private Optional<File> find(final File fromDir, final FilenameFilter filter) {
        if (testResourcesFolder.equals(fromDir)) {
            return Optional.empty();
        }
        File[] files = fromDir.listFiles(filter);
        if (Objects.nonNull(files) && files.length == 1) {
            return Optional.of(files[0]);
        }
        return find(fromDir.getParentFile(), filter);
    }

    public String searchFileToString(final String name, final File fromDir) {
        Preconditions.checkNotNull(fromDir);
        try {
            return FileUtils.readFileToString(searchFileFromDir(fromDir, name), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    public File searchFileFromDataFolder(final String fileName) {
        final String targetName = fileName.startsWith(SLASH_SEPARATOR)
                ? fileName.substring(1) : fileName;
        String finalFileName = targetName.split("\\s+")[0];
        File file = dataFolderFiles.get(finalFileName);
        if (Objects.isNull(file)) {
            throw new FileLinkingException(dataFolder, testResourcesFolder, finalFileName);
        }
        return file;
    }

    public Optional<File> searchFileFromEnvFolder(final String folder, final String fileName) {
        Map<String, File> files = envFoldersFiles.get(folder);
        if (Objects.isNull(files)) {
            throw new FileLinkingException(String.format(ENV_FOLDER_NOT_EXIST, folder), envFolder);
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

