package com.konstantion.dto.table.dto;

import com.konstantion.expedition.ExpeditionType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TableDto(
        UUID id,
        String name,
        Integer capacity,
        ExpeditionType tableType,
        UUID hallId,
        UUID orderId,
        Set<UUID> waitersId,
        LocalDateTime createdAt,
        LocalDateTime deletedAt,
        Boolean active
) {
}
