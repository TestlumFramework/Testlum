package com.knubisoft.e2e.testing.framework.configuration;

import lombok.Getter;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.COMPONENTS_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CREDENTIALS_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXCEL_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.FOLDER_LOCATION_ERROR_MESSAGE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.PAGES_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.PATCHES_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCENARIOS_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SHELL_FOLDER_NOT_EXIST;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VARIATIONS_FOLDER_NOT_EXIST;

@Getter
public class TestResourceSettings {

    public static final String SCENARIO_FILENAME = "scenario.xml";
    public static final String ACTUAL_FILENAME = "actual.json";
    public static final String SCREENSHOT_FILENAME = "screenshot.jpg";
    public static final String INIT_PATCH_FILENAME = "_init.sql";
    public static final String XML_SUFFIX = ".xml";
    public static final String SCREENSHOT_FOLDER = "/screenshots";
    public static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;
    public static final String SCREENSHOT_NAME_TO_SAVE = "%s_action_%s_" + SCREENSHOT_FILENAME;
    public static final String REPORT_TEMPLATE_PATH = "reportTemplate/index.html";
    public static final String REPORT_PATH = "/report/index-final.html";
    public static final String JS_FOLDER = "javascript";
    public static final String SCHEMAS_FOLDER = "schema";

    private static final String TEST_RESOURCES_FOLDER = "/src/test/resources";
    private static final String PAGES_FOLDER = "locators/pages";
    private static final String COMPONENTS_FOLDER = "locators/component";
    private static final String SCENARIOS_FOLDER = "scenarios";
    private static final String VARIATIONS_FOLDER = "variations";
    private static final String PATCHES_FOLDER = "patches";
    private static final String CREDS_FOLDER = "credentials";
    private static final String SHELL_FOLDER = "shell";
    private static final String EXCEL_FOLDER = "excel";

    private static TestResourceSettings instance;

    private final File testResourcesFolder;
    private final File pagesFolder;
    private final File componentsFolder;
    private final File scenariosFolder;
    private final File variationsFolder;
    private final File patchesFolder;
    private final File credentialsFolder;
    private final File configFile;
    private final File shellFolder;
    private final File excelFolder;

    private TestResourceSettings(final String configFileName, final String pathToTestResources) {
        this.testResourcesFolder = new File(pathToTestResources);
        this.configFile = new File(testResourcesFolder, configFileName);
        this.shellFolder = subFolder(SHELL_FOLDER, SHELL_FOLDER_NOT_EXIST);
        this.pagesFolder = subFolder(PAGES_FOLDER, PAGES_FOLDER_NOT_EXIST);
        this.componentsFolder = subFolder(COMPONENTS_FOLDER, COMPONENTS_FOLDER_NOT_EXIST);
        this.scenariosFolder = subFolder(SCENARIOS_FOLDER, SCENARIOS_FOLDER_NOT_EXIST);
        this.variationsFolder = subFolder(VARIATIONS_FOLDER, VARIATIONS_FOLDER_NOT_EXIST);
        this.patchesFolder = subFolder(PATCHES_FOLDER, PATCHES_FOLDER_NOT_EXIST);
        this.credentialsFolder = subFolder(CREDS_FOLDER, CREDENTIALS_FOLDER_NOT_EXIST);
        this.excelFolder = subFolder(EXCEL_FOLDER, EXCEL_FOLDER_NOT_EXIST);
    }

    public static void init(final String configFileName, final String pathToTestResources) {
        instance = new TestResourceSettings(configFileName, pathToTestResources);
    }

    public static TestResourceSettings getInstance() {
        return instance;
    }

    private File subFolder(final String name, final String errorMessage) {
        File folder = new File(testResourcesFolder, name);
        checkArgument(folder.exists(),
                String.format(FOLDER_LOCATION_ERROR_MESSAGE, errorMessage, folder.getAbsolutePath()));
        return folder;
    }
}
