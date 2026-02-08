package com.knubisoft.testlum.testing.framework.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.io.File.separator;

@Getter
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
    public static final String LOCATORS_PAGES_FOLDER = "locators" + separator + "pages";
    public static final String LOCATORS_COMPONENTS_FOLDER = "locators" + separator + "component";
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

    private static TestResourceSettings instance;

    @Getter
    @Setter
    private ApplicationContext ctx;

    private final File testResourcesFolder;
    private final File configFile;
    private final File envConfigFolder;
    private final File scenariosFolder;
    private final File dataFolder;
    private final Optional<File> scenarioScopeFolder;
    private File pagesFolder;
    private File componentsFolder;

    private TestResourceSettings(final String configFileName,
                                 final String pathToTestResources,
                                 final Optional<String> scenarioScope) {
        this.testResourcesFolder = new File(pathToTestResources);
        this.configFile = new File(testResourcesFolder, configFileName);
        this.envConfigFolder = subFolder(testResourcesFolder, ENV_CONFIG_FOLDER, ENV_CONFIG_FOLDER_NOT_EXIST);
        this.scenariosFolder = subFolder(testResourcesFolder, SCENARIOS_FOLDER, SCENARIOS_FOLDER_NOT_EXIST);
        this.dataFolder = subFolder(testResourcesFolder, DATA_FOLDER, DATA_FOLDER_NOT_EXIST);
        this.scenarioScopeFolder = scenarioScope
                .map(s -> subFolder(scenariosFolder, s, SPECIFIED_SCENARIOS_FOLDER_NOT_EXIST));
    }

    public static void init(
            final String configFileName,
            final String pathToTestResources,
            final Optional<String> scenarioScope) {
        TestResourceSettings.instance =
                new TestResourceSettings(configFileName, pathToTestResources, scenarioScope);
    }

    public void initLocatorsFolder() {
        this.pagesFolder = subFolder(testResourcesFolder, LOCATORS_PAGES_FOLDER, PAGES_FOLDER_NOT_EXIST);
        this.componentsFolder = subFolder(testResourcesFolder, LOCATORS_COMPONENTS_FOLDER, COMPONENTS_FOLDER_NOT_EXIST);
    }

    public static TestResourceSettings getInstance() {
        return TestResourceSettings.instance;
    }

    private File subFolder(final File sourceDirectory, final String name, final String errorMessage) {
        File folder = new File(sourceDirectory, name);
        checkArgument(folder.exists(),
                String.format(FOLDER_LOCATION_ERROR_MESSAGE, errorMessage, folder.getAbsolutePath()));
        return folder;
    }
}
