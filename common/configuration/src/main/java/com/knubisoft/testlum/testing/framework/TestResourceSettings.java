package com.knubisoft.testlum.testing.framework;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Optional;

@Getter
@Component
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
    public static final String REPORT_FOLDER = "report";

    @Getter
    private static String configFileName;
    @Getter
    private static String pathToTestResources;
    @Getter
    private static Optional<String> scenarioScope;
    private static boolean initialized;

    private final File testResourcesFolder;
    private final File configFile;
    private final File envConfigFolder;
    private final File scenariosFolder;
    private final File dataFolder;
    private final Optional<File> scenarioScopeFolder;
    private final File pagesFolder;
    private final File componentsFolder;

    public TestResourceSettings() {
        if (!initialized) {
            throw new RuntimeException("TestResourceSettings not initialized. Use init()");
        }

        this.testResourcesFolder = new File(pathToTestResources);
        Preconditions.checkArgument(testResourcesFolder.exists(), pathToTestResources + " does not exist");

        this.configFile = subFile(testResourcesFolder, configFileName);
        this.envConfigFolder = subFile(testResourcesFolder, "config");
        this.scenariosFolder = subFile(testResourcesFolder, "scenarios");
        this.dataFolder = subFile(testResourcesFolder, "data");
        this.scenarioScopeFolder = scenarioScope.map(name -> subFile(scenariosFolder, name));
        this.pagesFolder = subFile(testResourcesFolder, "locators" + File.separator + "pages");
        this.componentsFolder = subFile(testResourcesFolder, "locators" + File.separator + "component");
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

    private File subFile(final File sourceDirectory, final String name) {
        File folder = new File(sourceDirectory, name);
        String message = String.format("%s. Expected location -> %s",
                "[" + name + "] folder does not exist",
                folder.getAbsolutePath());
        Preconditions.checkArgument(folder.exists(), message);
        return folder;
    }
}

