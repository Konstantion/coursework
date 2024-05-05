package com.konstantion.expedition;

import com.konstantion.equipment.Equipment;
import com.konstantion.expedition.model.CreateTableRequest;
import com.konstantion.expedition.model.TableWaitersRequest;
import com.konstantion.expedition.model.UpdateTableRequest;
import com.konstantion.user.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface ExpeditionService extends UserDetailsService {
    List<Expedition> getAll(boolean onlyActive);

    default List<Expedition> getAll() {
        return getAll(true);
    }

    Expedition addWaiter(UUID tableId, TableWaitersRequest request, User user);

    Expedition removeWaiter(UUID tableId, TableWaitersRequest request, User user);

    Expedition update(UUID tableId, UpdateTableRequest request, User user);

    Expedition activate(UUID tableId, User user);

    Expedition deactivate(UUID tableId, User user);

    Expedition create(CreateTableRequest creationTable, User user);

    /**
     * This method isn't safe and delete entity in DB,
     * witch can lead to the destruction of relationships with other entities,
     * if you want to safely disable entity use {@link #deactivate(UUID, User)} instead.
     */
    Expedition delete(UUID tableId, User user);

    Expedition getById(UUID tableId);

    Equipment getOrderByTableId(UUID tableId);

    List<User> getWaitersByTableId(UUID tableId);

    Expedition removeAllWaiters(UUID tableId, User user);
}
