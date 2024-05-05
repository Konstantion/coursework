package com.konstantion.file.validator;

import com.konstantion.utils.validator.ValidationUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.isNull;

@Component
public record MultipartFileValidations() implements ValidationUtil {
    private static final Double BYTES_IN_MEGABYTE = 1e6;
    private static final String FILE = "file";

    private static final byte[][] IMAGE_SIGNATURES = {
            // JPEG
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0},
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1},
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB},

            // PNG
            {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47},

            // GIF
            {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38},

            // BMP
            {(byte) 0x42, (byte) 0x4D},

            // ICO
            {(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00},

            // TIFF
            {(byte) 0x49, (byte) 0x49, (byte) 0x2A, (byte) 0x00},
            {(byte) 0x4D, (byte) 0x4D, (byte) 0x00, (byte) 0x2A}
    };

    /**
     * Checks bits signature of the file
     */
    private static boolean deviousBitsCheck(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileSignature = new byte[4]; //get signature
            inputStream.read(fileSignature);

            for (byte[] signature : IMAGE_SIGNATURES) {
                if (matchesSignature(fileSignature, signature)) { // check it
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    private static boolean matchesSignature(byte[] fileSignature, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (fileSignature[i] != signature[i]) {
                return false;
            }
        }

        return true;
    }

    Optional<FieldError> isFileValid(MultipartFile file) {
        Set<String> errorSet = new HashSet<>();
        if (isNull(file) || file.getOriginalFilename().isEmpty()) {
            errorSet.add("File shouldn't be empty");
            return setToOptional(errorSet, file, FILE);
        }

        if (file.getSize() > 10 * BYTES_IN_MEGABYTE) {
            errorSet.add("File size should be less than 10 MB");
        }

        if (!file.getContentType().startsWith("image/")) {
            errorSet.add("File should be of type image");
        }

        if (!deviousBitsCheck(file)) {
            errorSet.add("I know that it's not an image :), stop!");
        }

        return setToOptional(errorSet, file, FILE);
    }
}
