package com.konstantion.camp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampPort {
    List<Camp> findAll();

    Optional<Camp> findById(UUID uuid);

    Camp save(Camp camp);

    void delete(Camp camp);

    void deleteAll();
}
