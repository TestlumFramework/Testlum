package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.ElementHighlighter;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;

import static java.lang.String.format;

public abstract class AbstractSeleniumInterpreter<T extends AbstractCommand> extends AbstractInterpreter<T> {

    protected AbstractSeleniumInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    protected WebElement findWebElement(final String locatorId) {
        return UiUtil.findWebElement(dependencies.getWebDriver(), locatorId);
    }

    protected void takeScreenshotAndSaveIfRequired(final CommandResult result) {
        if (dependencies.getGlobalTestConfiguration().getUi().getBrowserSettings()
                .getTakeScreenshotOfEachUiCommand().isEnable()) {
            File screenshot = UiUtil.takeScreenshot(dependencies.getWebDriver());
            File screenshotsFolder = new File(dependencies.getFile().getParent()
                    + TestResourceSettings.SCREENSHOT_FOLDER);
            tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder);
            UiUtil.putScreenshotToResult(result, screenshot);
        }
    }

    private void tryToCopyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder) {
        try {
            copyScreenshotFileToFolder(screenshot, screenshotsFolder);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    protected void highlightElementIfRequired(final Boolean isHighlight, final WebElement element) {
        if (isHighlight == null || isHighlight) {
            ElementHighlighter.highlight(element, dependencies.getWebDriver());
        }
    }

    private void copyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder) throws IOException {
        String screenshotFileName = format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                LocalTime.now(),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }
}
