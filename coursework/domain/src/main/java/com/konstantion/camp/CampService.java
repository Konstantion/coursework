package com.konstantion.camp;

import com.konstantion.camp.model.CreateHallRequest;
import com.konstantion.camp.model.UpdateHallRequest;
import com.konstantion.expedition.Expedition;
import com.konstantion.user.User;

import java.util.List;
import java.util.UUID;

public interface CampService {
    Camp create(CreateHallRequest createHallRequest, User user);

    Camp getById(UUID id);

    List<Camp> getAll(boolean onlyActive);

    default List<Camp> getAll() {
        return getAll(true);
    }

    Camp update(UUID id, UpdateHallRequest request, User user);

    Camp activate(UUID id, User user);

    Camp deactivate(UUID id, User user);

    Camp delete(UUID id, User user);

    default List<Expedition> getTablesByHallId(UUID id) {
        return getTablesByHallId(id, true);
    }

    List<Expedition> getTablesByHallId(UUID id, boolean onlyActive);
}
