package com.konstantion.expedition.model;

import java.util.UUID;

public record CreateTableRequest(
        String name,
        Integer capacity,
        String tableType,
        UUID hallId,
        String password
) {
}
