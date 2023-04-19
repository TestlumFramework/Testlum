package com.knubisoft.testlum.testing.framework.configuration;

import lombok.Getter;

import java.io.File;

import static com.google.common.base.Preconditions.checkArgument;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.COMPONENTS_FOLDER_NOT_EXIST;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DATA_FOLDER_NOT_EXIST;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ENV_CONFIG_FOLDER_NOT_EXIST;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.FOLDER_LOCATION_ERROR_MESSAGE;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.PAGES_FOLDER_NOT_EXIST;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.SCENARIOS_FOLDER_NOT_EXIST;
import static java.io.File.separator;

@Getter
public class TestResourceSettings {

    public static final String SCENARIO_FILENAME = "scenario.xml";
    public static final String INTEGRATION_CONFIG_FILENAME = "integration.xml";
    public static final String UI_CONFIG_FILENAME = "ui.xml";
    public static final String ACTUAL_FILENAME = "actual.json";
    public static final String ACTUAL_IMAGE_PREFIX = "actual_image_compared_to_";
    public static final String FILENAME_TO_SAVE = "action_%s_" + ACTUAL_FILENAME;
    public static final String SCREENSHOT_FILENAME = "screenshot.jpg";
    public static final String SCREENSHOT_NAME_TO_SAVE = "%s_action_%s_" + SCREENSHOT_FILENAME;
    public static final String XML_SUFFIX = ".xml";

    public static final String SCREENSHOT_FOLDER = "screenshots";
    public static final String SCHEMAS_FOLDER = "schema";
    public static final String REPORT_FOLDER = "report";
    public static final String LOCATORS_PAGES_FOLDER = "locators" + separator + "pages";
    public static final String LOCATORS_COMPONENTS_FOLDER = "locators" + separator + "component";
    public static final String SCENARIOS_FOLDER = "scenarios";
    public static final String DATA_FOLDER = "data";
    public static final String ENV_CONFIG_FOLDER = "config";

    private static TestResourceSettings instance;

    private final File testResourcesFolder;
    private final File configFile;
    private final File envConfigFolder;
    private final File pagesFolder;
    private final File componentsFolder;
    private final File scenariosFolder;
    private final File dataFolder;

    private TestResourceSettings(final String configFileName, final String pathToTestResources) {
        this.testResourcesFolder = new File(pathToTestResources);
        this.configFile = new File(testResourcesFolder, configFileName);
        this.envConfigFolder = subFolder(ENV_CONFIG_FOLDER, ENV_CONFIG_FOLDER_NOT_EXIST);
        this.pagesFolder = subFolder(LOCATORS_PAGES_FOLDER, PAGES_FOLDER_NOT_EXIST);
        this.componentsFolder = subFolder(LOCATORS_COMPONENTS_FOLDER, COMPONENTS_FOLDER_NOT_EXIST);
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
