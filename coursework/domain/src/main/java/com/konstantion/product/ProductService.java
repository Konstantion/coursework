package com.konstantion.product;

import com.konstantion.product.model.CreateProductRequest;
import com.konstantion.user.User;

import java.util.UUID;

public interface ProductService {
    Product create(CreateProductRequest cpdto, User user);

    /**
     * This method isn't safe and delete entity in DB,
     * witch can lead to the destruction of relationships with other entities,
     * if you want to safely disable entity use {@link #deactivate(UUID, User)} instead.
     */
    Product delete(UUID productId, User user);
    Product deactivate(UUID productId, User user);
    Product activate(UUID productId, User user);
    Product getById(UUID productId);
}