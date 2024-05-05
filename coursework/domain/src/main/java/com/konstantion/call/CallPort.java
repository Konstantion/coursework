package com.konstantion.call;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallPort {
    List<Call> findAll();

    Optional<Call> findById(UUID id);

    Call save(Call call);

    void delete(Call call);

    Optional<Call> findByTableIdAndPurpose(UUID tableId, Purpose purpose);

    void deleteAll();
}
