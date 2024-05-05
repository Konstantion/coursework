package com.konstantion.expedition;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpeditionPort {
    Expedition save(Expedition table);

    void delete(Expedition table);

    Optional<Expedition> findById(UUID id);

    List<Expedition> findAll();

    List<Expedition> findAllWhereHallId(UUID hallId);

    Optional<Expedition> findByName(String name);

    void deleteAll();
}
