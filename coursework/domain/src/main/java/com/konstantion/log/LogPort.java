package com.konstantion.log;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogPort {
    Optional<Log> findById(UUID id);

    Optional<Log> findByOrderId(UUID id);

    Log save(Log log);

    void delete(Log log);

    List<Log> findAll();

    void deleteAll();
}
