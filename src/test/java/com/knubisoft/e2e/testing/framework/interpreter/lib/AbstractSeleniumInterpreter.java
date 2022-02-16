package com.knubisoft.e2e.testing.framework.interpreter.lib;

import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.ElementHighlighter;
import com.knubisoft.e2e.testing.framework.util.ImageCompressor;
import com.knubisoft.e2e.testing.model.pages.Locator;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Base64;
import java.util.Objects;

import static java.lang.String.format;

public abstract class AbstractSeleniumInterpreter<T extends AbstractCommand> extends AbstractInterpreter<T> {

    protected AbstractSeleniumInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    protected void takeScreenshotIfRequired(final CommandResult result) {
        if (dependencies.getGlobalTestConfiguration().getUi().getBrowserSettings()
                .isScreenshotsLogging()) {
            File screenshot = ((TakesScreenshot) dependencies.getWebDriver()).getScreenshotAs(OutputType.FILE);
            File screenshotsFolder = new File(dependencies.getFile().getParent()
                    + TestResourceSettings.SCREENSHOT_FOLDER);
            tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder);
            putScreenshotToResult(result, screenshot);
        }
    }

    private void putScreenshotToResult(final CommandResult result, final File screenshot) {
        final MultipartFile image = ImageCompressor.compress(screenshot);
        if (Objects.nonNull(image)) {
            byte[] fileContent = getFileContentBytes(image);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            result.put("screenshot", encodedString);
        }
    }

    @NotNull
    private byte[] getFileContentBytes(final MultipartFile image) {
        byte[] fileContent;
        try {
            fileContent = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DefaultFrameworkException(e);
        }
        return fileContent;
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

    protected WebElement getWebElement(final String locatorId) {
        Locator locator = dependencies.getGlobalLocators().getLocator(locatorId);
        WebDriver webDriver = dependencies.getWebDriver();
        return dependencies.getWebElementFinder().find(locator, webDriver);
    }

    private void copyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder) throws IOException {
        String screenshotFileName = format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                LocalTime.now(),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }
}
