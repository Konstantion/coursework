package com.konstantion.gear.model;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UpdateProductRequest(
        String name,
        Double price,
        Double weight,
        MultipartFile image,
        String description,
        UUID categoryId
) {
}
