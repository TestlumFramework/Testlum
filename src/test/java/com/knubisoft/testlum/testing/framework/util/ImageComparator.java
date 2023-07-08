package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.model.scenario.CompareWith;
import com.knubisoft.testlum.testing.model.scenario.FindIn;
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
        if (nonNull(image.getCompareWith())) {
            setCompareWithMismatch(image.getCompareWith(), imageComparison);
        } else {
            setFindInMismatch(image.getFindIn(), imageComparison);
        }
        return imageComparison.compareImages();
    }

    private void setCompareWithMismatch(final CompareWith compareWith,
                                        final ImageComparison imageComparison) {
        if (nonNull(compareWith.getFullScreen()) && nonNull(compareWith.getFullScreen().getMismatch())) {
            imageComparison.setAllowingPercentOfDifferentPixels(compareWith.getFullScreen().getMismatch());
        } else if (nonNull(compareWith.getElement()) && nonNull(compareWith.getElement().getMismatch())) {
            imageComparison.setAllowingPercentOfDifferentPixels(compareWith.getElement().getMismatch());
        }
    }

    private void setFindInMismatch(final FindIn findIn, final ImageComparison imageComparison) {
        if (nonNull(findIn.getFullScreen()) && nonNull(findIn.getFullScreen().getMismatch())) {
            imageComparison.setAllowingPercentOfDifferentPixels(findIn.getFullScreen().getMismatch());
        } else if (nonNull(findIn.getElement()) && nonNull(findIn.getElement().getMismatch())) {
            imageComparison.setAllowingPercentOfDifferentPixels(findIn.getElement().getMismatch());
        }
    }
}
