package com.knubisoft.cott.testing.framework.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.CONFIG_EXAMPLE_FILENAME;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.DATA_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.EXAMPLE_TEST_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.EXPECTED_EXAMPLE_FILENAME;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.JS_SCRIPT_EXAMPLE_FILENAME;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.REQUIRED_FOLDER_NAMES;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.SCENARIO_FILENAME;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.SHELL_SCRIPT_EXAMPLE_FILENAME;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.TEST_SAMPLE_PATH;

@UtilityClass
public class InitialStructureGenerator {

    private static final Map<String, String> FOR_COPY_FILENAMES_TO_FOLDERS;

    static {
        final Map<String, String> map = new HashMap<>(5);
        map.put(CONFIG_EXAMPLE_FILENAME, "");
        map.put(SCENARIO_FILENAME, EXAMPLE_TEST_FOLDER);
        map.put(EXPECTED_EXAMPLE_FILENAME, EXAMPLE_TEST_FOLDER);
        map.put(JS_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        map.put(SHELL_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        FOR_COPY_FILENAMES_TO_FOLDERS = Collections.unmodifiableMap(map);
    }

    public void generate(final String pathToGenerate) {
        REQUIRED_FOLDER_NAMES.forEach(name ->
                createFolder(pathToGenerate, name));
        FOR_COPY_FILENAMES_TO_FOLDERS.forEach((fileName, folder) ->
                copyFileToFolder(fileName, pathToGenerate, folder));
    }

    @SneakyThrows
    private void createFolder(final String path, final String name) {
        File folder = new File(path, name);
        FileUtils.forceMkdir(folder);
    }

    @SneakyThrows
    private void copyFileToFolder(final String fileName, final String path, final String folder) {
        try (InputStream file = InitialStructureGenerator.class.getClassLoader()
                .getResourceAsStream(TEST_SAMPLE_PATH + fileName)) {
            if (file != null) {
                File target = Paths.get(path, folder, fileName).toFile();
                FileUtils.copyToFile(file, target);
            }
        }
    }
}
