package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.DATA_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.LOCATORS_COMPONENTS_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.LOCATORS_PAGES_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.REPORT_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.SCENARIOS_FOLDER;
import static com.knubisoft.cott.testing.framework.configuration.TestResourceSettings.SCENARIO_FILENAME;

@UtilityClass
public class InitialStructureGenerator {

    private static final String CONFIG_EXAMPLE_FILENAME = "global-config-example.xml";
    private static final String EXPECTED_EXAMPLE_FILENAME = "expected_1.json";
    private static final String JS_SCRIPT_EXAMPLE_FILENAME = "greating.js";
    private static final String SHELL_SCRIPT_EXAMPLE_FILENAME = "shell-1.sh";
    private static final String TEST_SAMPLE_PATH = "initial-sample";
    private static final String EXAMPLE_TEST_FOLDER = SCENARIOS_FOLDER + File.separator + "default";

    private static final List<String> REQUIRED_FOLDER_NAMES = Collections.unmodifiableList(Arrays.asList(
            SCENARIOS_FOLDER, LOCATORS_PAGES_FOLDER, LOCATORS_COMPONENTS_FOLDER, REPORT_FOLDER, DATA_FOLDER));

    private static final Map<String, String> FOR_COPY_FILENAMES_TO_FOLDERS;

    static {
        final Map<String, String> map = new HashMap<>(5);
        map.put(CONFIG_EXAMPLE_FILENAME, DelimiterConstant.EMPTY);
        map.put(SCENARIO_FILENAME, EXAMPLE_TEST_FOLDER);
        map.put(EXPECTED_EXAMPLE_FILENAME, EXAMPLE_TEST_FOLDER);
        map.put(JS_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        map.put(SHELL_SCRIPT_EXAMPLE_FILENAME, DATA_FOLDER);
        FOR_COPY_FILENAMES_TO_FOLDERS = Collections.unmodifiableMap(map);
    }

    public void generate(final String pathToGenerate) {
        try {
            execute(pathToGenerate);
            LogUtil.logStructureGeneration(pathToGenerate);
        } catch (IOException e) {
            LogUtil.logErrorStructureGeneration(pathToGenerate, e);
        }
    }

    private void execute(final String pathToGenerate) throws IOException {
        for (String name : REQUIRED_FOLDER_NAMES) {
            createFolder(pathToGenerate, name);
        }
        for (Map.Entry<String, String> each : FOR_COPY_FILENAMES_TO_FOLDERS.entrySet()) {
            copyFileToFolder(each.getKey(), pathToGenerate, each.getValue());
        }
    }

    private void createFolder(final String path, final String name) throws IOException {
        File folder = new File(path, name);
        FileUtils.forceMkdir(folder);
    }

    private void copyFileToFolder(final String fileName, final String path, final String folder) throws IOException {
        try (InputStream file = InitialStructureGenerator.class.getClassLoader()
                .getResourceAsStream(TEST_SAMPLE_PATH + File.separator + fileName)) {
            if (Objects.nonNull(file)) {
                File target = Paths.get(path, folder, fileName).toFile();
                FileUtils.copyToFile(file, target);
            }
        }
    }
}
