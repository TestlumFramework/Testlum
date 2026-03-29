package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageCompressorTest {

    @TempDir
    File tempDir;

    private final ImageCompressor compressor = new ImageCompressor();

    private File createTestImage(final int width, final int height) throws IOException {
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final File file = new File(tempDir, "test_" + width + "x" + height + ".jpeg");
        ImageIO.write(image, "jpeg", file);
        return file;
    }

    @Nested
    class SmallImages {
        @Test
        void smallImageNotResized() throws IOException {
            final File smallImage = createTestImage(100, 100);
            final MultipartFile result = compressor.compress(smallImage);
            assertNotNull(result);
            assertTrue(result.getBytes().length > 0);
        }

        @Test
        void exactLimitNotResized() throws IOException {
            final File exactImage = createTestImage(600, 400);
            final MultipartFile result = compressor.compress(exactImage);
            assertNotNull(result);
        }
    }

    @Nested
    class LargeImages {
        @Test
        void largeImageIsResized() throws IOException {
            final File largeImage = createTestImage(1200, 800);
            final MultipartFile result = compressor.compress(largeImage);
            assertNotNull(result);
            assertTrue(result.getBytes().length > 0);
        }

        @Test
        void wideImageIsResized() throws IOException {
            final File wideImage = createTestImage(900, 300);
            final MultipartFile result = compressor.compress(wideImage);
            assertNotNull(result);
        }
    }

    @Nested
    class InvalidInput {
        @Test
        void nonExistentFileThrows() {
            final File nonExistent = new File(tempDir, "does_not_exist.jpeg");
            assertThrows(DefaultFrameworkException.class,
                    () -> compressor.compress(nonExistent));
        }
    }
}
