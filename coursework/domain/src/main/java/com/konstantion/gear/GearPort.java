package com.konstantion.gear;

import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface GearPort {
    Gear save(Gear gear);

    void delete(Gear gear);

    Optional<Gear> findById(UUID id);

    Page<Gear> findAll(
            int pageNumber,
            int pageSize,
            String orderBy,
            String searchPattern,
            UUID categoryId,
            boolean ascending,
            boolean isActive
    );

    void deleteAll();
}
