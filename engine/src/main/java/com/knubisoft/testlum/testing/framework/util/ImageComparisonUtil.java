package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.exception.ImageComparisonException;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;

@RequiredArgsConstructor
@Component
public class ImageComparisonUtil {

    private static final String MOBILE_SCREEN_HEIGHT = "return screen.height;";
    private static final String WINDOW_INNER_HEIGHT = "return window.innerHeight;";
    private static final String STATUS_BAR_HEIGHT = "statBarHeight";

    private final UiUtil uiUtil;
    private final ResultUtil resultUtil;
    private final JavascriptUtil javascriptUtil;

    public void processImageComparisonResult(final ImageComparisonResult comparisonResult,
                                             final String expectedImageFullName,
                                             final boolean isHighlightDifference,
                                             final File directoryToSave,
                                             final CommandResult result) {
        if (comparisonResult.getImageComparisonState() != ImageComparisonState.MATCH) {
            saveAndAttachImage(comparisonResult, expectedImageFullName, isHighlightDifference, directoryToSave, result);
            throwMismatchException(comparisonResult, result);
        }
    }

    private void saveAndAttachImage(final ImageComparisonResult comparisonResult,
                                    final String expectedImageFullName,
                                    final boolean isHighlightDifference,
                                    final File directoryToSave,
                                    final CommandResult result) {
        try {
            File actualImage = saveActualImage(
                    comparisonResult, expectedImageFullName, isHighlightDifference, directoryToSave);
            uiUtil.putScreenshotToResult(result, actualImage);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
        result.put(ResultUtil.ADDITIONAL_INFO, ResultUtil.IMAGE_ATTACHED_TO_STEP);
    }

    private void throwMismatchException(final ImageComparisonResult comparisonResult, final CommandResult result) {
        if (comparisonResult.getImageComparisonState().equals(ImageComparisonState.SIZE_MISMATCH)) {
            processSizeMismatchException(comparisonResult, result);
        } else {
            result.put(ResultUtil.IMAGE_MISMATCH_PERCENT, comparisonResult.getDifferencePercent());
            throw new ImageComparisonException(String.format(ExceptionMessage.IMAGES_MISMATCH,
                    comparisonResult.getDifferencePercent()));
        }
    }

    private void processSizeMismatchException(final ImageComparisonResult comparisonResult,
                                              final CommandResult result) {
        resultUtil.addImagesSizeMetaData(comparisonResult, result);
        throw new ImageComparisonException(String.format(ExceptionMessage.IMAGES_SIZE_MISMATCH,
                comparisonResult.getExpected().getWidth(), comparisonResult.getExpected().getHeight(),
                comparisonResult.getActual().getWidth(), comparisonResult.getActual().getHeight()));
    }

    private File saveActualImage(final ImageComparisonResult comparisonResult,
                                 final String expectedImageFullName,
                                 final boolean isHighlightDifference,
                                 final File directoryToSave) throws IOException {
        String imageExtension = FilenameUtils.getExtension(expectedImageFullName);
        File fileToSave = getFileToSave(directoryToSave, expectedImageFullName);
        if (isHighlightDifference) {
            ImageIO.write(comparisonResult.getResult(), imageExtension, fileToSave);
        } else {
            ImageIO.write(comparisonResult.getActual(), imageExtension, fileToSave);
        }
        return fileToSave;
    }

    private File getFileToSave(final File directoryToSave, final String expectedImageFullName) {
        verifyDirectoryToSave(directoryToSave);
        return new File(directoryToSave.getAbsolutePath(), getImageNameToSave(expectedImageFullName));
    }

    private void verifyDirectoryToSave(final File directoryToSave) {
        if (Objects.isNull(directoryToSave) || !directoryToSave.exists() || !directoryToSave.isDirectory()) {
            throw new ImageComparisonException(String.format("[%s] doesn't exist or not directory",
                    directoryToSave.getAbsolutePath()));
        }
    }

    private String getImageNameToSave(final String expectedImageFullName) {
        return String.format("%s%s.%s", TestResourceSettings.ACTUAL_IMAGE_PREFIX,
                FilenameUtils.getBaseName(expectedImageFullName),
                FilenameUtils.getExtension(expectedImageFullName));
    }

    public int getStatusBarHeight(final WebDriver driver) {
        Capabilities deviceCapabilities = ((RemoteWebDriver) driver).getCapabilities();
        Object capability = deviceCapabilities.getCapability(STATUS_BAR_HEIGHT);
        if (Objects.nonNull(capability)) {
            return (int) ((long) capability);
        }
        long screenHeight = (Long) javascriptUtil.executeJsScript(MOBILE_SCREEN_HEIGHT, driver);
        long windowHeight = (Long) javascriptUtil.executeJsScript(WINDOW_INNER_HEIGHT, driver);
        return (int) (screenHeight - windowHeight);
    }
}
