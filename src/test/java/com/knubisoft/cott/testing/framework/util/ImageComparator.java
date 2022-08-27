package com.knubisoft.cott.testing.framework.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;

@UtilityClass
public class ImageComparator {

    public ImageComparisonResult compare(final BufferedImage expectedImage, final BufferedImage actualImage) {
        return new ImageComparison(expectedImage, actualImage).compareImages();
    }
}
