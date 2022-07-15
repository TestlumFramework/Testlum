package com.knubisoft.e2e.testing.framework.util;

import com.google.common.base.Preconditions;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.exception.FileLinkingException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.DUPLICATE_FILENAME;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.FILE_NOT_EXIST;

@AllArgsConstructor
@Getter
@Slf4j
public final class FileSearcher {

    private static final Map<String, File> DATA_FOLDER_FILES;
    static {
        DATA_FOLDER_FILES = collectFilesFromDataFolder();
    }

    private final File root;
    private final File fromDir;

    public FileSearcher(final File root) {
        this(root, null);
    }

    public File search(final File fromDir, final String name) {
        final String targetName = name.startsWith(DelimiterConstant.SLASH_SEPARATOR) ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        return find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, root, name));
    }

    public File search(final String name) {
        Preconditions.checkNotNull(fromDir);
        return search(fromDir, name);
    }


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
    public String searchFileToString(final String name) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(search(fromDir, name), StandardCharsets.UTF_8);
    }

    public File fileByNameAndExtension(final String fileName) {
        final String targetName = fileName.startsWith(DelimiterConstant.SLASH_SEPARATOR)
                ? fileName.substring(1) : fileName;
        File file = DATA_FOLDER_FILES.get(targetName);
        if (Objects.isNull(file)) {
            throw new DefaultFrameworkException(FILE_NOT_EXIST, fileName,
                    TestResourceSettings.getInstance().getDataFolder().getAbsolutePath());
        }
        return file;
    }

    private static Map<String, File> collectFilesFromDataFolder() {
        Map<String, File> files = new HashMap<>();
        FileUtils.listFiles(TestResourceSettings.getInstance().getDataFolder(), null, true)
                .forEach(file -> {
                    files.computeIfPresent(file.getName(), (key, value) -> {
                        throw new DefaultFrameworkException(DUPLICATE_FILENAME, file.getName());
                    });
                    files.put(file.getName(), file);
                });
        return Collections.unmodifiableMap(files);
    }
}
