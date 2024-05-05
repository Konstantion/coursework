package com.konstantion.equipment;

import com.konstantion.equipment.model.OrderProductsRequest;
import com.konstantion.gear.Gear;
import com.konstantion.user.User;

import java.util.List;
import java.util.UUID;

public interface EquipmentService {
    List<Equipment> getAll(boolean onlyActive);

    default List<Equipment> getAll() {
        return getAll(true);
    }

    Equipment getById(UUID id);

    Equipment transferToAnotherTable(UUID orderId, UUID tableId, User user);

    Equipment open(UUID tableId, User user);

    Equipment close(UUID orderId, User user);

    Equipment delete(UUID orderId, User user);

    List<Gear> getProductsByOrderId(UUID orderId);

    int addProduct(UUID orderId, OrderProductsRequest request, User user);

    int removeProduct(UUID orderId, OrderProductsRequest request, User user);
}
