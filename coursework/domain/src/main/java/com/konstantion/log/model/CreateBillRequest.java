package com.konstantion.log.model;

import java.util.UUID;

public record CreateBillRequest(
        UUID orderId,
        UUID guestId
) {
}
