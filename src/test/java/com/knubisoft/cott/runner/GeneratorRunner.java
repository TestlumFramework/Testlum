package com.knubisoft.cott.runner;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.util.ArgumentsUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.DATA_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.LOCATORS_COMPONENTS_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.LOCATORS_PAGES_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.REPORT_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.SCENARIOS_FOLDER;

public class GeneratorRunner implements COTTStarter.Runner {

    private static final List<String> REQUIRED_FOLDER_NAMES = Arrays.asList(
            SCENARIOS_FOLDER, LOCATORS_PAGES_FOLDER, LOCATORS_COMPONENTS_FOLDER, REPORT_FOLDER, DATA_FOLDER);

    private static final String SCENARIO_EXAMPLE_FILENAME = TestResourceSettings.SCENARIO_FILENAME;
    private static final String CONFIG_EXAMPLE_FILENAME = "global-config-example.xml";
    private static final String EXPECTED_EXAMPLE_FILENAME = "expected_1.json";
    private static final String JS_SCRIPT_EXAMPLE_FILENAME = "greating.js";
    private static final String SHELL_SCRIPT_EXAMPLE_FILENAME = "shell-1.sh";
    private static final String TEST_SAMPLE_PATH = "initial-sample/";

    private static final Map<String, String> FOR_COPY_FILENAMES_TO_FOLDERS = prepareMap();

    private static Map<String, String> prepareMap() {
        Map<String, String> map = new HashMap<>();
        map.put(CONFIG_EXAMPLE_FILENAME, "");
        map.put(SCENARIO_EXAMPLE_FILENAME, SCENARIOS_FOLDER);
        map.put(EXPECTED_EXAMPLE_FILENAME, SCENARIOS_FOLDER);
        map.put(JS_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        map.put(SHELL_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void run(final String[] args) {
        String pathToInitialStructure = ArgumentsUtils.getPathToInitialStructureGeneration(args[0]);
        REQUIRED_FOLDER_NAMES.forEach(name ->
                createFolder(pathToInitialStructure, name));
        FOR_COPY_FILENAMES_TO_FOLDERS.forEach((fileName, folder) ->
                copyFileToFolder(fileName, pathToInitialStructure, folder));
    }

    @SneakyThrows
    private void createFolder(final String path, final String name) {
        File folder = new File(path, name);
        FileUtils.forceMkdir(folder);
    }

    @SneakyThrows
    private void copyFileToFolder(final String fileName, final String path, final String folder) {
        try (InputStream file = this.getClass().getClassLoader()
                .getResourceAsStream(TEST_SAMPLE_PATH + fileName)) {
            if (file != null) {
                File target = Paths.get(path, folder, fileName).toFile();
                FileUtils.copyToFile(file, target);
            }
        }
    }
}
