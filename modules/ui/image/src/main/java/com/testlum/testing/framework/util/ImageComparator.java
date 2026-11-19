package com.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.model.scenario.Image;
import com.testlum.testing.model.scenario.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class ImageComparator {

    private static final double MAX_PERCENT = 100;
    private static final int OPACITY_PERCENT = 50;

    public ImageComparisonResult compare(final Image image,
                                         final BufferedImage expectedImage,
                                         final BufferedImage actualImage,
                                         final List<Rectangle> excludes) {
        BufferedImage expected = toRgb(expectedImage);
        BufferedImage actual = toRgb(actualImage);
        if (isFullScreen(image)) {
            if (expected.getWidth() != actual.getWidth() || expected.getHeight() != actual.getHeight()) {
                log.warn(LogMessage.ACTUAL_IMAGE_SIZE_DIFFERS_LOG,
                        actual.getWidth(), actual.getHeight(), expected.getWidth(), expected.getHeight());
            }
            actual = scaleToWidth(actual, expected.getWidth());
            int height = Math.min(expected.getHeight(), actual.getHeight());
            expected = cropToHeight(expected, height);
            actual = cropToHeight(actual, height);
        }
        return getImageComparisonResult(image.getFullScreen(), image.getPart(), expected, actual, excludes);
    }

    public ImageComparisonResult compare(final MobileImage image,
                                         final BufferedImage expectedImage,
                                         final BufferedImage actualImage) {
        return getImageComparisonResult(image.getFullScreen(), image.getPart(), expectedImage, actualImage, List.of());
    }

    public ImageComparisonResult compare(final NativeImage image,
                                         final BufferedImage expectedImage,
                                         final BufferedImage actualImage) {
        return getImageComparisonResult(image.getFullScreen(), image.getPart(), expectedImage, actualImage, List.of());
    }

    private ImageComparisonResult getImageComparisonResult(final FullScreen fullScreen,
                                                           final Part part,
                                                           final BufferedImage expectedImage,
                                                           final BufferedImage actualImage,
                                                           final List<Rectangle> excludedElements) {
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
        if (!excludedElements.isEmpty()) {
            setExcludedElements(excludedElements, imageComparison);
        }
        if (Objects.nonNull(fullScreen) && Objects.nonNull(fullScreen.getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(MAX_PERCENT - fullScreen.getPercentage());
        } else if (Objects.nonNull(part) && Objects.nonNull(part.getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(MAX_PERCENT - part.getPercentage());
        }
        return imageComparison.compareImages();
    }

    private void setExcludedElements(final List<Rectangle> excludedElements,
                                     final ImageComparison imageComparison) {
        imageComparison.setExcludedAreas(excludedElements);
        imageComparison.setDrawExcludedRectangles(true);
        imageComparison.setExcludedRectangleFilling(true, OPACITY_PERCENT);
        imageComparison.setExcludedRectangleColor(Color.ORANGE);
    }

    private boolean isFullScreen(final Image image) {
        return Objects.isNull(image.getPicture()) && Objects.isNull(image.getPart());
    }

    private BufferedImage toRgb(final BufferedImage image) {
        return image.getType() == BufferedImage.TYPE_INT_RGB
                ? image
                : draw(image, image.getWidth(), image.getHeight());
    }

    private BufferedImage scaleToWidth(final BufferedImage image, final int width) {
        if (image.getWidth() == width) {
            return image;
        }
        int height = Math.max(1, Math.round((float) image.getHeight() * width / image.getWidth()));
        return draw(image, width, height);
    }

    private BufferedImage cropToHeight(final BufferedImage image, final int height) {
        return image.getHeight() == height ? image : image.getSubimage(0, 0, image.getWidth(), height);
    }

    private BufferedImage draw(final BufferedImage image, final int width, final int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();
        return result;
    }
}
