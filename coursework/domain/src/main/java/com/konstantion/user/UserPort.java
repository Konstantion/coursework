package com.konstantion.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    User save(User user);

    List<User> findAll();

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    void delete(User user);

    void deleteAll();
}