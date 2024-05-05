package com.konstantion.equipment.model;

import java.util.UUID;

public record OrderProductsRequest(
        UUID productId,
        int quantity
) {
}
