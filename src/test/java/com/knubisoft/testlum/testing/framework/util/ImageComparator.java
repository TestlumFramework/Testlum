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

    public ImageComparisonResult compare(final BufferedImage expectedImage,
                                         final BufferedImage actualImage,
                                         final List<Rectangle> excludedElements,
                                         final Image image) {
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
        if (nonNull(image.getCompareWithFullScreen())) {
            if (!excludedElements.isEmpty()) {
                imageComparison.setExcludedAreas(excludedElements);
            }
            if (nonNull(image.getCompareWithFullScreen().getMismatch())) {
                imageComparison.setAllowingPercentOfDifferentPixels(image.getCompareWithFullScreen().getMismatch());
            }
        }
        return imageComparison.compareImages();
    }
}
