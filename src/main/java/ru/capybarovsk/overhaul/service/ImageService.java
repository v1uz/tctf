package ru.capybarovsk.overhaul.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;

import javax.imageio.ImageIO;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {
    private static final float MAX_IMAGE_SIZE = 768.0F;
    private static final String OUTPUT_FORMAT = "jpg";
    private static final String OUTPUT_MIME = "image/jpeg";

    @Value("${overhaul.storeImages}")
    private String storeImagesProp;
    private Path storeImagesPath;

    public ImageService() {
    }

    @PostConstruct
    private void initialize() {
        storeImagesPath = Path.of(storeImagesProp);
        if (!storeImagesPath.toFile().exists() && !storeImagesPath.toFile().mkdirs()) {
            throw new RuntimeException("Could not create " + storeImagesPath.toAbsolutePath());
        }
    }

    /**
     * @param file source image
     * @return data-URL of resized image
     * @throws IOException if image cannot be read or unsupported
     */
    public String scaleAndSave(String requestId, MultipartFile file) throws IOException {
        final BufferedImage original;
        try (InputStream is = file.getInputStream()) {
            original = ImageIO.read(is);
        }

        float scaleFactor = Math.min(
                1.0F,
                Math.min(MAX_IMAGE_SIZE / original.getWidth(), MAX_IMAGE_SIZE / original.getHeight())
        );

        final BufferedImage target;
        if (scaleFactor < 1.0F) {
            int width = Math.round(scaleFactor * original.getWidth());
            int height = Math.round(scaleFactor * original.getHeight());

            Image resized = original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = target.createGraphics();
            g2d.drawImage(resized, 0, 0, null);
            g2d.dispose();
        } else {
            target = original;
        }

        ImageIO.write(target, OUTPUT_FORMAT, storeImagesPath.resolve(requestId + ".jpg").toFile());

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(target, OUTPUT_FORMAT, os);
            os.flush();

            return "data:" + OUTPUT_MIME + ";base64," + Base64.getEncoder().encodeToString(os.toByteArray());
        }
    }
}
