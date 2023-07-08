package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.model.scenario.CompareWithFullScreen;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;
import java.util.List;

import static java.util.Objects.nonNull;

@UtilityClass
public class ImageComparator {

    public ImageComparisonResult compare(final BufferedImage expectedImage,
                                         final BufferedImage actualImage,
                                         final List<Rectangle> excludedElements,
                                         final CompareWithFullScreen compareWithFullScreen) {
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage);
        if (nonNull(compareWithFullScreen)) {
            if (!excludedElements.isEmpty()) {
                imageComparison.setExcludedAreas(excludedElements);
            }
            if (nonNull(compareWithFullScreen.getMismatch())) {
                imageComparison.setAllowingPercentOfDifferentPixels(compareWithFullScreen.getMismatch());
            }
        }
        return imageComparison.compareImages();
    }
}
