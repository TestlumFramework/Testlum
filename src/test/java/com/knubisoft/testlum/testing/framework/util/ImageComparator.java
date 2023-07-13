package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.model.scenario.Image;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;
import java.util.List;

import static java.util.Objects.nonNull;

@UtilityClass
public class ImageComparator {

    public ImageComparisonResult compare(final Image image,
                                         final BufferedImage expectedImage,
                                         final BufferedImage actualImage,
                                         final List<Rectangle> excludedElements) {
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
        if (!excludedElements.isEmpty()) {
            imageComparison.setExcludedAreas(excludedElements);
        }
        if (nonNull(image.getFullScreen()) && nonNull(image.getFullScreen().getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(image.getFullScreen().getPercentage());
        } else if (nonNull(image.getPart()) && nonNull(image.getPart().getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(image.getPart().getPercentage());
        } else if (nonNull(image.getFindPart()) && nonNull(image.getFindPart().getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(image.getFindPart().getPercentage());
        }
        return imageComparison.compareImages();
    }
}
