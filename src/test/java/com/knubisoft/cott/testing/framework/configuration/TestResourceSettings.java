package com.knubisoft.cott.testing.framework.configuration;

import lombok.Getter;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.COMPONENTS_FOLDER_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DATA_FOLDER_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FOLDER_LOCATION_ERROR_MESSAGE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.PAGES_FOLDER_NOT_EXIST;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCENARIOS_FOLDER_NOT_EXIST;

@Getter
public class TestResourceSettings {

    public static final String SCENARIO_FILENAME = "scenario.xml";
    public static final String ACTUAL_FILENAME = "actual.json";
    public static final String SCREENSHOT_FILENAME = "screenshot.jpg";
    public static final String XML_SUFFIX = ".xml";
    public static final String SCREENSHOT_FOLDER = "/screenshots";
    public static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;
    public static final String SCREENSHOT_NAME_TO_SAVE = "%s_action_%s_" + SCREENSHOT_FILENAME;
    public static final String SCHEMAS_FOLDER = "schema";
    public static final String REPORT_FOLDER = "/report";

    private static final String PAGES_FOLDER = "locators/pages";
    private static final String COMPONENTS_FOLDER = "locators/component";
    private static final String SCENARIOS_FOLDER = "scenarios";
    private static final String DATA_FOLDER = "data";

    private static TestResourceSettings instance;

    private final File testResourcesFolder;
    private final File pagesFolder;
    private final File componentsFolder;
    private final File scenariosFolder;
    private final File configFile;
    private final File dataFolder;

    private TestResourceSettings(final String configFileName, final String pathToTestResources) {
        this.testResourcesFolder = new File(pathToTestResources);
        this.configFile = new File(testResourcesFolder, configFileName);
        this.pagesFolder = subFolder(PAGES_FOLDER, PAGES_FOLDER_NOT_EXIST);
        this.componentsFolder = subFolder(COMPONENTS_FOLDER, COMPONENTS_FOLDER_NOT_EXIST);
        this.scenariosFolder = subFolder(SCENARIOS_FOLDER, SCENARIOS_FOLDER_NOT_EXIST);
        this.dataFolder = subFolder(DATA_FOLDER, DATA_FOLDER_NOT_EXIST);
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
