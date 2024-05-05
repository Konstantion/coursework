package com.konstantion.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryPort {
    List<Category> findAll();

    Optional<Category> findById(UUID id);

    Category save(Category category);

    void delete(Category category);

    void deleteAll();
}
