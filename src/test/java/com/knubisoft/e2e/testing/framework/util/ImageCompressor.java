package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class ImageCompressor {
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String EXTENSION_JPEG = "jpeg";
    private static final String IMAGE_PNG = "image/png";
    private static final Integer WIDTH_IMAGE_SIZE = 600;
    private static final Integer HEIGHT_IMAGE_SIZE = 400;

    public MultipartFile compress(final File originalImage) {
        final BufferedImage image;
        final MultipartFile multipartFile = tryToGetMultipart(originalImage);
        image = getBufferedImage(multipartFile);
        return image.getWidth() == WIDTH_IMAGE_SIZE && image.getHeight() == HEIGHT_IMAGE_SIZE
                ? multipartFile : resizeImage(multipartFile.getName(), image);
    }

    @NotNull
    private MultipartFile tryToGetMultipart(final File originalImage) {
        final MultipartFile multipartFile;
        try {
            multipartFile = getMultipartFile(originalImage);
        } catch (IOException e) {
            throw new DefaultFrameworkException("Image processing error, please recheck the screenshot", e);
        }
        return multipartFile;
    }

    private BufferedImage getBufferedImage(final MultipartFile multipartFile) {
        final BufferedImage image;
        try {
            image = ImageIO.read(multipartFile.getInputStream());
        } catch (IOException e) {
            throw new DefaultFrameworkException("Image processing error, please recheck the screenshot", e);
        }
        return image;
    }

    @NotNull
    private MultipartFile getMultipartFile(final File originalImage) throws IOException {
        final byte[] fileInByteArray;
        final MultipartFile multipartFile;
        fileInByteArray = FileUtils.readFileToByteArray(originalImage);
        multipartFile = new MockMultipartFile("image",
                originalImage.getName(), IMAGE_JPEG, fileInByteArray);
        return multipartFile;
    }

    private MultipartFile resizeImage(final String name, final BufferedImage avatar) {
        final Image originalAvatar = avatar.getScaledInstance(WIDTH_IMAGE_SIZE, HEIGHT_IMAGE_SIZE,
                Image.SCALE_AREA_AVERAGING);
        final BufferedImage resizedAvatar = new BufferedImage(WIDTH_IMAGE_SIZE, HEIGHT_IMAGE_SIZE,
                BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resizedAvatar.getGraphics().drawImage(originalAvatar, 0, 0, null);
        writeImage(resizedAvatar, byteArrayOutputStream);
        return new MockMultipartFile(name, byteArrayOutputStream.toByteArray());
    }

    private void writeImage(final BufferedImage resizedAvatar, final ByteArrayOutputStream byteArrayOutputStream) {
        try {
            ImageIO.write(resizedAvatar, EXTENSION_JPEG, byteArrayOutputStream);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }
}
