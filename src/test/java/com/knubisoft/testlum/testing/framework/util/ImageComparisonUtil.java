package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.exception.ImageComparisonException;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IMAGES_MISMATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IMAGES_SIZE_MISMATCH;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ADDITIONAL_INFO;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.IMAGE_ATTACHED_TO_STEP;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.IMAGE_MISMATCH_PERCENT;
import static java.lang.String.format;

@UtilityClass
public class ImageComparisonUtil {

    @SneakyThrows
    public void processImageComparisonResult(final ImageComparisonResult comparisonResult,
                                             final String expectedImageFullName,
                                             final boolean isHighlightDifference,
                                             final File directoryToSave,
                                             final CommandResult result) {
        ImageComparisonState imageComparisonState = comparisonResult.getImageComparisonState();
        if (imageComparisonState != ImageComparisonState.MATCH) {
            File actualImage =
                    saveActualImage(comparisonResult, expectedImageFullName, isHighlightDifference, directoryToSave);
            UiUtil.putScreenshotToResult(result, actualImage);
            result.put(ADDITIONAL_INFO, IMAGE_ATTACHED_TO_STEP);
            if (imageComparisonState.equals(ImageComparisonState.SIZE_MISMATCH)) {
                processSizeMismatchException(comparisonResult, result);
            } else {
                result.put(IMAGE_MISMATCH_PERCENT, comparisonResult.getDifferencePercent());
                throw new ImageComparisonException(format(IMAGES_MISMATCH, comparisonResult.getDifferencePercent()));
            }
        }
    }

    private void processSizeMismatchException(final ImageComparisonResult comparisonResult,
                                              final CommandResult result) {
        ResultUtil.addImagesSizeMetaData(comparisonResult, result);
        throw new ImageComparisonException(format(IMAGES_SIZE_MISMATCH,
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
            throw new ImageComparisonException(format("[%s] doesn't exist or not directory",
                    directoryToSave.getAbsolutePath()));
        }
    }

    private String getImageNameToSave(final String expectedImageFullName) {
        return format("%s%s.%s", TestResourceSettings.ACTUAL_IMAGE_PREFIX,
                FilenameUtils.getBaseName(expectedImageFullName),
                FilenameUtils.getExtension(expectedImageFullName));
    }
}
