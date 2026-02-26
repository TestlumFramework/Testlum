package com.knubisoft.testlum.testing.framework.configuration;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

@Getter
@Component
@Setter
public class TestResourceSettings {

    public static final String SCENARIO_FILENAME = "scenario.xml";
    public static final String INTEGRATION_CONFIG_FILENAME = "integration.xml";
    public static final String UI_CONFIG_FILENAME = "ui.xml";
    public static final String ACTUAL_FILENAME = "actual.json";
    public static final String ACTUAL_IMAGE_PREFIX = "actual_image_compared_to_";
    public static final String SCREENSHOT_FILENAME = "screenshot.jpg";
    public static final String SCREENSHOT_NAME_TO_SAVE = "%s_action_%s_" + SCREENSHOT_FILENAME;
    public static final String XML_SUFFIX = ".xml";

    public static final String SCREENSHOT_FOLDER = "screenshots";
    public static final String SCHEMAS_FOLDER = "schema";
    public static final String REPORT_FOLDER = "report";
    public static final String LOCATORS_PAGES_FOLDER = "locators" + File.separator + "pages";
    public static final String LOCATORS_COMPONENTS_FOLDER = "locators" + File.separator + "component";
    public static final String SCENARIOS_FOLDER = "scenarios";
    public static final String DATA_FOLDER = "data";
    public static final String ENV_CONFIG_FOLDER = "config";
    public static final String FOLDER_LOCATION_ERROR_MESSAGE = "%s. Expected location -> %s";

    private static final String SCENARIOS_FOLDER_NOT_EXIST = "[scenarios] folder does not exist";
    private static final String SPECIFIED_SCENARIOS_FOLDER_NOT_EXIST = "[specified scenarios] folder does not exist";
    private static final String PAGES_FOLDER_NOT_EXIST = "[locators/pages] folder does not exist";
    private static final String COMPONENTS_FOLDER_NOT_EXIST = "[locators/component] folder does not exist";
    private static final String DATA_FOLDER_NOT_EXIST = "[data] folder does not exist";
    private static final String ENV_CONFIG_FOLDER_NOT_EXIST = "[config] folder does not exist";

    @Getter
    private static String configFileName;
    @Getter
    private static String pathToTestResources;
    @Getter
    private static Optional<String> scenarioScope;
    private static boolean initialized = false;

    private final File testResourcesFolder;

    @Getter
    private final File configFile;
    private final File envConfigFolder;
    private final File scenariosFolder;
    private final File dataFolder;
    private final Optional<File> scenarioScopeFolder;
    private File pagesFolder;
    private File componentsFolder;

    public TestResourceSettings() {
        if (!initialized) {
            throw new RuntimeException("TestResourceSettings not initialized. Use init()");
        }

        this.testResourcesFolder = new File(pathToTestResources);
        this.configFile = new File(testResourcesFolder, configFileName);
        this.envConfigFolder = subFolder(testResourcesFolder, ENV_CONFIG_FOLDER, ENV_CONFIG_FOLDER_NOT_EXIST);
        this.scenariosFolder = subFolder(testResourcesFolder, SCENARIOS_FOLDER, SCENARIOS_FOLDER_NOT_EXIST);
        this.dataFolder = subFolder(testResourcesFolder, DATA_FOLDER, DATA_FOLDER_NOT_EXIST);
        this.scenarioScopeFolder = scenarioScope
                .map(s -> subFolder(scenariosFolder, s, SPECIFIED_SCENARIOS_FOLDER_NOT_EXIST));
        this.pagesFolder = subFolder(testResourcesFolder, LOCATORS_PAGES_FOLDER, PAGES_FOLDER_NOT_EXIST);
        this.componentsFolder = subFolder(testResourcesFolder, LOCATORS_COMPONENTS_FOLDER, COMPONENTS_FOLDER_NOT_EXIST);
    }

    public static void init(
            final String configFileName,
            final String pathToTestResources,
            final Optional<String> scenarioScope) {
        TestResourceSettings.initialized = true;
        TestResourceSettings.configFileName = configFileName;
        TestResourceSettings.pathToTestResources = pathToTestResources;
        TestResourceSettings.scenarioScope = scenarioScope;
    }

    private File subFolder(final File sourceDirectory, final String name, final String errorMessage) {
        File folder = new File(sourceDirectory, name);
        String message = String.format(FOLDER_LOCATION_ERROR_MESSAGE, errorMessage, folder.getAbsolutePath());
        Preconditions.checkArgument(folder.exists(), message);
        return folder;
    }
}
