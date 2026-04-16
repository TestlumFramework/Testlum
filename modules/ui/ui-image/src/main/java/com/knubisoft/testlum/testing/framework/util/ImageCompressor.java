package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Service
public class ImageCompressor {

    private static final String EXTENSION_JPEG = "jpeg";
    private static final Integer WIDTH_IMAGE_SIZE = 600;
    private static final Integer HEIGHT_IMAGE_SIZE = 400;

    public byte[] compress(final File originalImage) {
        final byte[] fileBytes = readFileBytes(originalImage);
        final BufferedImage image = getBufferedImage(originalImage);
        return (image.getWidth() <= WIDTH_IMAGE_SIZE && image.getHeight() <= HEIGHT_IMAGE_SIZE)
                ? fileBytes : resizeImage(image);
    }

    private byte[] readFileBytes(final File originalImage) {
        try {
            return FileUtils.readFileToByteArray(originalImage);
        } catch (IOException e) {
            throw new DefaultFrameworkException("Image processing error, please recheck the screenshot", e);
        }
    }

    private BufferedImage getBufferedImage(final File imageFile) {
        try {
            return ImageIO.read(imageFile);
        } catch (IOException e) {
            throw new DefaultFrameworkException("Image processing error, please recheck the screenshot", e);
        }
    }

    private byte[] resizeImage(final BufferedImage avatar) {
        final Image originalAvatar = avatar.getScaledInstance(WIDTH_IMAGE_SIZE, HEIGHT_IMAGE_SIZE,
                Image.SCALE_AREA_AVERAGING);
        final BufferedImage resizedAvatar = new BufferedImage(WIDTH_IMAGE_SIZE, HEIGHT_IMAGE_SIZE,
                BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedAvatar.getGraphics().drawImage(originalAvatar, 0, 0, null);
        writeImage(resizedAvatar, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void writeImage(final BufferedImage resizedAvatar, final ByteArrayOutputStream byteArrayOutputStream) {
        try {
            ImageIO.write(resizedAvatar, EXTENSION_JPEG, byteArrayOutputStream);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }
}
