package com.grandhorizon.util;

import jakarta.servlet.http.Part;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;

public final class RoomImageStorage {
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 6000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;

    private RoomImageStorage() {
    }

    public static String save(Part part) throws IOException, InvalidImageException {
        if (part == null || part.getSize() == 0) {
            return null;
        }
        if (part.getSize() > MAX_IMAGE_SIZE) {
            throw new InvalidImageException("Room images must be 5 MB or smaller.");
        }

        BufferedImage image;
        try (ImageInputStream input = ImageIO.createImageInputStream(part.getInputStream())) {
            if (input == null) {
                throw new InvalidImageException("Upload a valid PNG or JPEG room image.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidImageException("Upload a valid PNG or JPEG room image.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName();
                if (!"png".equalsIgnoreCase(format) && !"jpeg".equalsIgnoreCase(format)
                        && !"jpg".equalsIgnoreCase(format)) {
                    throw new InvalidImageException("Only PNG and JPEG room images are supported.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new InvalidImageException("Room images must be no larger than 6000 by 6000 pixels.");
                }
                image = reader.read(0);
            } catch (IOException e) {
                throw new InvalidImageException("The uploaded file could not be decoded as an image.", e);
            } finally {
                reader.dispose();
            }
        }
        if (image == null) {
            throw new InvalidImageException("Upload a valid PNG or JPEG room image.");
        }

        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", encoded)) {
            throw new IOException("Unable to encode the uploaded room image.");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + ".png";
        Path directory = imageDirectory();
        Path destination = directory.resolve(fileName);
        Path temporary = Files.createTempFile(directory, "room-image-", ".tmp");
        try {
            Files.write(temporary, encoded.toByteArray());
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return fileName;
    }

    public static Path find(String fileName) throws IOException {
        if (fileName == null || !fileName.matches("[0-9a-f]{32}\\.png")) {
            return null;
        }
        Path image = imageDirectory().resolve(fileName).normalize();
        if (!image.startsWith(imageDirectory()) || !Files.isRegularFile(image)) {
            return null;
        }
        return image;
    }

    public static void delete(String fileName) throws IOException {
        if (fileName == null || !fileName.matches("[0-9a-f]{32}\\.png")) {
            return;
        }
        Files.deleteIfExists(imageDirectory().resolve(fileName));
    }

    private static Path imageDirectory() throws IOException {
        Path directory = FileStorageUtil.getDataDirectory().resolve("room-images").normalize();
        Files.createDirectories(directory);
        return directory;
    }

    public static class InvalidImageException extends Exception {
        public InvalidImageException(String message) {
            super(message);
        }

        public InvalidImageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
