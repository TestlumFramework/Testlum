package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.exception.ImageComparisonException;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Image;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import nu.pattern.OpenCV;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IMAGES_MISMATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IMAGES_SIZE_MISMATCH;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.IMAGE_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ADDITIONAL_INFO;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.IMAGE_ATTACHED_TO_STEP;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.IMAGE_MISMATCH_PERCENT;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.String.format;

@UtilityClass
public class ImageComparisonUtil {

    private static final int BYTES_PER_PIXEL = 3;
    private static final int LEAST_SIGNIFICANT_BYTE = 0xFF;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int BITS_16 = 16;
    private static final int BITS_8 = 8;
    private static final int RGB_255 = 255;

    @SneakyThrows
    public void processImageComparisonResult(final ImageComparisonResult comparisonResult,
                                             final Image image,
                                             final File directoryToSave,
                                             final CommandResult result) {
        ImageComparisonState imageComparisonState = comparisonResult.getImageComparisonState();
        if (imageComparisonState != ImageComparisonState.MATCH) {
            File actualImageFile = saveActualImage(comparisonResult, image, directoryToSave);
            UiUtil.putScreenshotToResult(result, actualImageFile);
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
                                 final Image image,
                                 final File directoryToSave) throws IOException {
        String expectedImageFullName = image.getFile();
        String imageExtension = FilenameUtils.getExtension(expectedImageFullName);
        File fileToSave = getFileToSave(directoryToSave, expectedImageFullName);
        if (image.isHighlightDifference()) {
            ImageIO.write(comparisonResult.getResult(), imageExtension, fileToSave);
        } else {
            ImageIO.write(comparisonResult.getActual(), imageExtension, fileToSave);
        }
        return fileToSave;
    }

    private File getFileToSave(final File directoryToSave, final String expectedImageFullName) {
        verifyDirectoryToSave(directoryToSave);
        return new File(directoryToSave.getAbsolutePath(),
                getImageNameToSave(expectedImageFullName, TestResourceSettings.ACTUAL_IMAGE_PREFIX));
    }

    private void verifyDirectoryToSave(final File directoryToSave) {
        if (Objects.isNull(directoryToSave) || !directoryToSave.exists() || !directoryToSave.isDirectory()) {
            throw new ImageComparisonException(format("[%s] doesn't exist or not directory",
                    directoryToSave.getAbsolutePath()));
        }
    }

    private String getImageNameToSave(final String expectedImageFullName, final String imagePrefix) {
        return format("%s%s.%s", imagePrefix,
                FilenameUtils.getBaseName(expectedImageFullName),
                FilenameUtils.getExtension(expectedImageFullName));
    }

    public void findExpectedInActual(final Image image,
                                     final BufferedImage expected,
                                     final BufferedImage actual,
                                     final File parentFile,
                                     final List<Rectangle> excludedElements,
                                     final CommandResult result) {
        OpenCV.loadShared();
        Mat actualMat = bufferedImageToMat(actual);
        Mat expectedMat = bufferedImageToMat(expected);
        Mat resultMat = new Mat();
        Imgproc.matchTemplate(actualMat, expectedMat, resultMat, Imgproc.TM_CCOEFF);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);
        Rect matchRect = new Rect(mmr.maxLoc, new Size(expectedMat.width(), expectedMat.height()));
        BufferedImage subImage = actual.getSubimage(matchRect.x, matchRect.y, matchRect.width, matchRect.height);
        ImageComparisonResult comparisonResult = ImageComparator.compare(image, expected, subImage, excludedElements);
        saveResultImage(image, comparisonResult.getImageComparisonState(), parentFile, actualMat, matchRect, result);
    }

    private Mat bufferedImageToMat(final BufferedImage imageToConvert) {
        if (imageToConvert.getType() == TYPE_INT_RGB || imageToConvert.getType() == TYPE_INT_ARGB) {
            int[] data = ((DataBufferInt) imageToConvert.getRaster().getDataBuffer()).getData();
            Mat matImage = new Mat(imageToConvert.getHeight(), imageToConvert.getWidth(), CvType.CV_8UC3);
            matImage.put(0, 0, intArrayToByteArray(data));
            Mat matImageRGB = new Mat();
            Imgproc.cvtColor(matImage, matImageRGB, Imgproc.COLOR_BGR2RGB);
            return matImageRGB;
        } else {
            BufferedImage convertedImage =
                    new BufferedImage(imageToConvert.getWidth(), imageToConvert.getHeight(), TYPE_INT_RGB);
            convertedImage.getGraphics().drawImage(imageToConvert, 0, 0, null);
            return bufferedImageToMat(convertedImage);
        }
    }

    private byte[] intArrayToByteArray(final int[] data) {
        byte[] byteArray = new byte[data.length * BYTES_PER_PIXEL];
        for (int i = 0; i < data.length; i++) {
            int pixel = data[i];
            byteArray[i * BYTES_PER_PIXEL] = (byte) ((pixel >> BITS_16) & LEAST_SIGNIFICANT_BYTE);
            byteArray[i * BYTES_PER_PIXEL + ONE] = (byte) ((pixel >> BITS_8) & LEAST_SIGNIFICANT_BYTE);
            byteArray[i * BYTES_PER_PIXEL + TWO] = (byte) (pixel & LEAST_SIGNIFICANT_BYTE);
        }
        return byteArray;
    }

    private void saveResultImage(final Image image,
                                 final ImageComparisonState comparisonState,
                                 final File parentFile,
                                 final Mat actualMat,
                                 final Rect matchRect,
                                 final CommandResult result) {
        verifyDirectoryToSave(parentFile);
        if (comparisonState.equals(ImageComparisonState.MATCH)) {
            Imgproc.rectangle(actualMat, matchRect.tl(), matchRect.br(), new Scalar(0, RGB_255, 0), 1);
            saveImage(image.getFile(), parentFile, actualMat, TestResourceSettings.RESULT_IMAGE_PREFIX, result);
        } else {
            Imgproc.rectangle(actualMat, matchRect.tl(), matchRect.br(), new Scalar(0, 0, RGB_255), 1);
            saveImage(image.getFile(), parentFile, actualMat, TestResourceSettings.ACTUAL_IMAGE_PREFIX, result);
            throw new ImageComparisonException(IMAGE_NOT_FOUND);
        }
    }

    private void saveImage(final String expectedImageName,
                           final File parentFile,
                           final Mat actualMat,
                           final String imagePrefix,
                           final CommandResult result) {
        File resultFile = new File(parentFile.getAbsolutePath(), getImageNameToSave(expectedImageName, imagePrefix));
        Imgcodecs.imwrite(resultFile.getAbsolutePath(), actualMat);
        UiUtil.putScreenshotToResult(result, resultFile);
        result.put(ADDITIONAL_INFO, IMAGE_ATTACHED_TO_STEP);
    }
}
