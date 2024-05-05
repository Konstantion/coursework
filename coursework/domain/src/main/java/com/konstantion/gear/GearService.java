package com.konstantion.gear;

import com.konstantion.gear.model.CreateProductRequest;
import com.konstantion.gear.model.GetProductsRequest;
import com.konstantion.gear.model.UpdateProductRequest;
import com.konstantion.user.User;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface GearService {
    Gear create(CreateProductRequest createProductRequest, User user);

    Page<Gear> getAll(GetProductsRequest getProductsRequest, boolean onlyActive);

    default Page<Gear> getAll(GetProductsRequest getProductsRequest) {
        return getAll(getProductsRequest, true);
    }

    /**
     * This method isn't safe and delete entity in DB,
     * witch can lead to the destruction of relationships with other entities,
     * if you want to safely disable entity use {@link #deactivate(UUID, User)} instead.
     */
    Gear delete(UUID productId, User user);

    Gear update(UUID productId, UpdateProductRequest updateProductRequest, User user);

    Gear deactivate(UUID productId, User user);

    Gear activate(UUID productId, User user);

    Gear getById(UUID productId);
}
