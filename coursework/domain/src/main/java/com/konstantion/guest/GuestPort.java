package com.konstantion.guest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuestPort {
    List<Guest> findAll();

    Optional<Guest> findById(UUID uuid);

    Guest save(Guest guest);

    void delete(Guest guest);

    Optional<Guest> findByName(String name);

    void deleteAll();
}
