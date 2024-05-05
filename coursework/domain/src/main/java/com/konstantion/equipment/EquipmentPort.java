package com.konstantion.equipment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentPort {
    Equipment save(Equipment equipment);

    Optional<Equipment> findById(UUID id);

    void delete(Equipment equipment);

    List<Equipment> findAll();

    void deleteAll();
}
