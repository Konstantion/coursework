package com.konstantion.log;

import com.konstantion.log.model.CreateBillRequest;
import com.konstantion.user.User;

import java.util.List;
import java.util.UUID;

public interface LogService {
    List<Log> getAll(boolean onlyActive);

    default List<Log> getAll() {
        return getAll(true);
    }

    Log getById(UUID id);

    Log create(CreateBillRequest createBillRequest, User user);

    Log cancel(UUID billId, User user);

    Log close(UUID billId, User user);

    Log activate(UUID billId, User user);

    byte[] getPdfBytesById(UUID id, User user);
}
