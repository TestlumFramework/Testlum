package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.model.scenario.Element;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import lombok.experimental.UtilityClass;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import static java.util.Objects.nonNull;

@UtilityClass
public class ImageComparator {

    private static final double MAX_PERCENT = 100;
    private static final int OPACITY_PERCENT = 50;

    public ImageComparisonResult compare(final FullScreen fullScreen,
                                         final Element element,
                                         final BufferedImage expectedImage,
                                         final BufferedImage actualImage,
                                         final List<Rectangle> excludedElements) {
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
        if (!excludedElements.isEmpty()) {
            setExcludedElements(excludedElements, imageComparison);
        }
        if (nonNull(fullScreen) && nonNull(fullScreen.getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(MAX_PERCENT - fullScreen.getPercentage());
        } else if (nonNull(element) && nonNull(element.getPercentage())) {
            imageComparison.setAllowingPercentOfDifferentPixels(MAX_PERCENT - element.getPercentage());
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
}
