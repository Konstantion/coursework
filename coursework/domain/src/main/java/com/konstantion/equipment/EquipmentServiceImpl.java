package com.konstantion.equipment;

import com.konstantion.equipment.model.OrderProductsRequest;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.log.Log;
import com.konstantion.log.LogPort;
import com.konstantion.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.ADD_GEAR_TO_EQUIPMENT;
import static com.konstantion.user.Permission.CLOSE_EQUIPMENT;
import static com.konstantion.user.Permission.DELETE_EQUIPMENT;
import static com.konstantion.user.Permission.DELETE_GEAR_FROM_EQUIPMENT;
import static com.konstantion.user.Permission.OPEN_EQUIPMENT;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.TRANSFER_EQUIPMENT;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@Component
public record EquipmentServiceImpl(
        ExpeditionPort expeditionPort,
        GearPort gearPort,
        EquipmentPort equipmentPort,
        LogPort logPort
) implements EquipmentService {
    private static final Logger logger = LoggerFactory.getLogger(EquipmentService.class);

    @Override
    public List<Equipment> getAll(boolean onlyActive) {
        List<Equipment> equipment = equipmentPort.findAll();
        if (onlyActive) {
            return equipment.stream().filter(Equipment::isActive).toList();
        }
        logger.info("All orders successfully returned");
        return equipment;
    }

    @Override
    public Equipment getById(UUID id) {
        Equipment equipment = getByIdOrThrow(id);
        logger.info("Order with id {} successfully returned", id);
        return equipment;
    }

    @Override
    public Equipment transferToAnotherTable(UUID orderId, UUID tableId, User user) {
        if (user.hasNoPermission(TRANSFER_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Equipment equipment = getByIdOrThrow(orderId);
        ExceptionUtils.isActiveOrThrow(equipment);

        Expedition newTable = expeditionPort.findById(tableId)
                .orElseThrow(nonExistingIdSupplier(Expedition.class, tableId));
        ExceptionUtils.isActiveOrThrow(newTable);

        if (newTable.hasOrder()) {
            throw new BadRequestException(format("Table with id %s, already has active order with id %s", newTable.getId(), newTable.getEquipmentId()));
        }

        if (nonNull(equipment.getExpeditionId())) {
            Expedition oldTable = expeditionPort().findById(equipment.getExpeditionId())
                    .orElseThrow(nonExistingIdSupplier(Expedition.class, equipment.getExpeditionId()));
            oldTable.removeOrder();
            expeditionPort.save(oldTable);
        }

        equipment.setExpeditionId(newTable.getId());
        newTable.setEquipmentId(equipment.getId());

        equipmentPort.save(equipment);
        expeditionPort.save(newTable);

        logger.info("Order with id {} successfully transferred to the table with id {}", orderId, tableId);
        return equipment;
    }

    @Override
    public Equipment open(UUID tableId, User user) {
        if (user.hasNoPermission(OPEN_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Expedition table = expeditionPort.findById(tableId)
                .orElseThrow(nonExistingIdSupplier(Expedition.class, tableId));
        ExceptionUtils.isActiveOrThrow(table);

        if (table.hasOrder()) {
            throw new BadRequestException(format("Table with id %s already has order with id %s", table.getId(), table.getEquipmentId()));
        }

        Equipment equipment = Equipment.builder()
                .userId(user.getId())
                .expeditionId(table.getId())
                .gearsId(new ArrayList<>())
                .createdAt(now())
                .active(true)
                .build();


        equipmentPort.save(equipment);

        table.setEquipmentId(equipment.getId());
        expeditionPort.save(table);

        return equipment;
    }

    @Override
    public Equipment close(UUID orderId, User user) {
        if (user.hasNoPermission(CLOSE_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Equipment equipment = getByIdOrThrow(orderId);
        ExceptionUtils.isActiveOrThrow(equipment);

        if (equipment.getGearsId().isEmpty()) {
            equipmentPort.delete(equipment);
            logger.warn("Order with id {} didn't contain any product, so it was successfully deleted and returned", orderId);
            return equipment;
        }

        if (!equipment.hasBill()) {
            throw new BadRequestException(format("Order with id %s doesn't have a bill", equipment.getId()));
        }

        Log log = logPort.findById(equipment.getLogId())
                .orElseThrow(nonExistingIdSupplier(Log.class, equipment.getLogId()));
        if (log.isActive()) {
            throw new BadRequestException(format("Order with id %s has a bill with id %s that has not been payed", equipment.getId(), log.getId()));
        }

        if (nonNull(equipment.getExpeditionId())) {
            Expedition table = expeditionPort.findById(equipment.getExpeditionId())
                    .orElseThrow(nonExistingIdSupplier(Expedition.class, equipment.getExpeditionId()));

            if (table.hasOrder()
                    && table.getEquipmentId().equals(equipment.getId())) {
                table.removeOrder();
                expeditionPort.save(table);
            }
        }

        prepareToClose(equipment);
        equipmentPort.save(equipment);

        logger.info("Order with id {} successfully closed and returned", equipment);
        return equipment;
    }

    @Override
    public Equipment delete(UUID orderId, User user) {
        if (user.hasNoPermission(DELETE_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Equipment equipment = getByIdOrThrow(orderId);

        equipmentPort.delete(equipment);

        logger.info("Order with id {} successfully deleted and returned", orderId);
        return equipment;
    }

    /**
     * Fast method to get order products <b>without image bytes</b>!
     * <p> n - products</p>
     * <p> k - unique products | k є o(n)</p>
     * <p> db(x) - fetch image from database time;</p>
     * <p> db(1) + Θ(n) + Θ(n + db(k)) + Θ(n) = O(n + db(k))</p>
     */
    @Override
    public List<Gear> getProductsByOrderId(UUID orderId) {
        Equipment equipment = getByIdOrThrow(orderId);
        List<UUID> productIds = equipment.getGearsId();
        Map<UUID, Gear> productsMap = new HashSet<>(productIds).stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            Gear gear = gearPort.findById(id).orElseThrow();
                            gear.setImageBytes(null);
                            return gear;
                        }
                ));
        List<Gear> gears = productIds.stream()
                .map(productsMap::get)
                .toList();
        logger.info("Products from order with id {} successfully returned", orderId);
        return gears;
    }

    @Override
    public int addProduct(UUID orderId, OrderProductsRequest request, User user) {
        if (user.hasNoPermission(ADD_GEAR_TO_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        UUID productId = request.productId();
        int quantity = request.quantity();

        Equipment equipment = getByIdOrThrow(orderId);
        ExceptionUtils.isActiveOrThrow(equipment);

        if (equipment.hasBill()) {
            throw new BadRequestException(format("Products can't be added to the order with id %s because it already has a bill with id %s", equipment.getId(), equipment.getLogId()));
        }

        Gear gear = gearPort.findById(productId)
                .orElseThrow(nonExistingIdSupplier(Gear.class, productId));
        ExceptionUtils.isActiveOrThrow(gear);

        if (quantity <= 0) {
            throw new BadRequestException(format("Quantity should be greater than 0, given %s", quantity));
        }
        long theoreticalQuantity = equipment.getGearsId().stream().filter(id -> id.equals(gear.getId())).count() + quantity;
        if (theoreticalQuantity > 1 << 10) {
            throw new BadRequestException(format("Total quantity of product in order should not be greater than 1024, given %s", theoreticalQuantity));
        }

        int counter = 0;
        for (; counter < quantity; counter++) {
            equipment.getGearsId().add(gear.getId());
        }

        equipmentPort.save(equipment);
        logger.info("{} product(s) with id {} successfully added to the order with id {}", counter, productId, orderId);
        return counter;
    }

    @Override
    public int removeProduct(UUID orderId, OrderProductsRequest request, User user) {
        if (user.hasNoPermission(DELETE_GEAR_FROM_EQUIPMENT)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        UUID productId = request.productId();
        int quantity = request.quantity();

        Equipment equipment = getByIdOrThrow(orderId);
        ExceptionUtils.isActiveOrThrow(equipment);

        if (equipment.hasBill()) {
            throw new BadRequestException(format("Products can't be removed from the order with id %s because it already has a bill with id %s", equipment.getId(), equipment.getLogId()));
        }

        Gear gear = gearPort.findById(productId)
                .orElseThrow(nonExistingIdSupplier(Gear.class, productId));

        if (quantity <= 0) {
            throw new BadRequestException(format("Quantity should be greater than 0, given %s", quantity));
        }
        int counter = 0;
        for (; counter < quantity; counter++) {
            if (!equipment.getGearsId().remove(gear.getId())) {
                break;
            }
        }
        equipmentPort.save(equipment);
        logger.info("{} product(s) with id {} successfully removed from the order with id {}", counter, productId, orderId);
        return counter;
    }

    private Equipment getByIdOrThrow(UUID id) {
        return equipmentPort.findById(id)
                .orElseThrow(nonExistingIdSupplier(Equipment.class, id));
    }

    private void prepareToClose(Equipment equipment) {
        equipment.setClosedAt(now());
        equipment.setActive(false);
    }
}
