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

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Slf4j
public final class FileSearcher {

    private static final Map<String, File> DATA_FILES;
    static {
        Map<String, File> files = getDataFileset();
        DATA_FILES = Collections.unmodifiableMap(files);
    }

    private final File root;
    private final File fromDir;
    private final boolean overridePathToVolume;

    public FileSearcher(final File root, final boolean overridePathToVolume) {
        this(root, null, overridePathToVolume);
    }

    public File search(final File fromDir, final String name) {
        final String targetName = name.startsWith(DelimiterConstant.SLASH_SEPARATOR) ? name.substring(1) : name;
        FilenameFilter filter = (dir, file) -> file.equals(targetName);
        File result = find(fromDir, filter).orElseThrow(() -> new FileLinkingException(fromDir, root, name));
        if (overridePathToVolume) {
            return pathToVolume(result);
        }
        return result;
    }

    public File search(final String name) {
        Preconditions.checkNotNull(fromDir);
        return search(fromDir, name);
    }

    private File pathToVolume(final File result) {
        if (result.exists() && isFileAnImage(result)) {
            final String search = "src/test/resources";
            int idx = result.getAbsolutePath().indexOf(search) + search.length();
            File mounted = new File("/opt/src/test/resources", result.getAbsolutePath().substring(idx));
            if (!mounted.exists()) {
                throw new RuntimeException("File not found. Checked paths " + result.getAbsolutePath() + "  and  "
                        + mounted.getAbsolutePath());
            }
            return mounted;
        }
        return result;
    }

    private boolean isFileAnImage(final File result) {
        String mimetype = new MimetypesFileTypeMap().getContentType(result);
        String type = mimetype.split(DelimiterConstant.SLASH_SEPARATOR)[0];
        return "image".equals(type);
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
    public String searchFileAndReadToString(final String name) {
        return FileUtils.readFileToString(fileByNameAndExtension(name), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public String searchFileToString(final String name) {
        Preconditions.checkNotNull(fromDir);
        return FileUtils.readFileToString(search(fromDir, name), StandardCharsets.UTF_8);
    }

    public File fileByNameAndExtension(final String fileName) {
        File file = DATA_FILES.get(fileName);
        if (!file.exists()) {
            throw new FileLinkingException(TestResourceSettings.getInstance().getDataFolder(),
                    TestResourceSettings.getInstance().getTestResourcesFolder(), fileName);
        }
        return file;
    }

    private static Map<String, File> getDataFileset() {
        Map<String, File> map = new HashMap<>();
       FileUtils.listFiles(TestResourceSettings.getInstance().getDataFolder(), null, true)
               .forEach(o -> {
                   if (map.containsKey(o.getName())) {
                       throw new DefaultFrameworkException("There are the same filename in data subdirectories");
                   } else {
                       map.put(o.getName(), o);
                   }
               });
       return map;
    }
}
