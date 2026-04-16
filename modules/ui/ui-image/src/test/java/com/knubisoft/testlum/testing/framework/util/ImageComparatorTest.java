package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImageComparatorTest {

    private final ImageComparator comparator = new ImageComparator();

    private BufferedImage createSolidImage(final int width, final int height, final Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    @Nested
    class CompareWebImage {
        @Test
        void matchingImagesReturnMatch() {
            BufferedImage img = createSolidImage(100, 100, Color.RED);
            Image image = mock(Image.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            ImageComparisonResult result = comparator.compare(image, img, img, List.of());
            assertNotNull(result);
            assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState());
        }

        @Test
        void differentImagesReturnMismatch() {
            BufferedImage expected = createSolidImage(100, 100, Color.RED);
            BufferedImage actual = createSolidImage(100, 100, Color.BLUE);
            Image image = mock(Image.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            ImageComparisonResult result = comparator.compare(image, expected, actual, List.of());
            assertNotNull(result);
            assertEquals(ImageComparisonState.MISMATCH, result.getImageComparisonState());
        }

        @Test
        void withPercentageThresholdOnFullScreen() {
            BufferedImage actual = createSolidImage(100, 100, Color.RED);
            actual.setRGB(0, 0, Color.BLUE.getRGB());
            Image image = mock(Image.class);
            WebFullScreen fullScreen = mock(WebFullScreen.class);
            when(fullScreen.getPercentage()).thenReturn(99.0);
            when(image.getFullScreen()).thenReturn(fullScreen);
            when(image.getPart()).thenReturn(null);
            BufferedImage expected = createSolidImage(100, 100, Color.RED);
            ImageComparisonResult result = comparator.compare(image, expected, actual, List.of());
            assertNotNull(result);
        }

        @Test
        void withPercentageThresholdOnPart() {
            BufferedImage actual = createSolidImage(100, 100, Color.RED);
            actual.setRGB(0, 0, Color.BLUE.getRGB());
            Image image = mock(Image.class);
            Part part = mock(Part.class);
            when(part.getPercentage()).thenReturn(99.0);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(part);
            BufferedImage expected = createSolidImage(100, 100, Color.RED);
            ImageComparisonResult result = comparator.compare(image, expected, actual, List.of());
            assertNotNull(result);
        }

        @Test
        void withExcludes() {
            BufferedImage expected = createSolidImage(100, 100, Color.RED);
            BufferedImage actual = createSolidImage(100, 100, Color.BLUE);
            Image image = mock(Image.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            List<Rectangle> excludes = List.of(new Rectangle(0, 0, 100, 100));
            ImageComparisonResult result = comparator.compare(image, expected, actual, excludes);
            assertNotNull(result);
        }
    }

    @Nested
    class CompareMobileImage {
        @Test
        void matchingImagesReturnMatch() {
            BufferedImage img = createSolidImage(100, 100, Color.GREEN);
            MobileImage image = mock(MobileImage.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            ImageComparisonResult result = comparator.compare(image, img, img);
            assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState());
        }
    }

    @Nested
    class CompareNativeImage {
        @Test
        void matchingImagesReturnMatch() {
            BufferedImage img = createSolidImage(100, 100, Color.BLUE);
            NativeImage image = mock(NativeImage.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            ImageComparisonResult result = comparator.compare(image, img, img);
            assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState());
        }

        @Test
        void sizeMismatchReturnsSizeMismatch() {
            BufferedImage expected = createSolidImage(100, 100, Color.RED);
            BufferedImage actual = createSolidImage(200, 200, Color.RED);
            NativeImage image = mock(NativeImage.class);
            when(image.getFullScreen()).thenReturn(null);
            when(image.getPart()).thenReturn(null);
            ImageComparisonResult result = comparator.compare(image, expected, actual);
            assertEquals(ImageComparisonState.SIZE_MISMATCH, result.getImageComparisonState());
        }
    }
}
