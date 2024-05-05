package com.konstantion.expedition;

import com.konstantion.camp.Camp;
import com.konstantion.camp.CampPort;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.equipment.EquipmentService;
import com.konstantion.exception.ActiveStateException;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.expedition.model.CreateTableRequest;
import com.konstantion.expedition.model.TableWaitersRequest;
import com.konstantion.expedition.model.UpdateTableRequest;
import com.konstantion.expedition.validator.ExpeditionValidator;
import com.konstantion.user.User;
import com.konstantion.user.UserPort;
import com.konstantion.utils.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.ASSIGN_GUIDE_TO_EXPEDITION;
import static com.konstantion.user.Permission.CHANGE_EXPEDITION_STATE;
import static com.konstantion.user.Permission.CREATE_EXPEDITION;
import static com.konstantion.user.Permission.DELETE_EXPEDITION;
import static com.konstantion.user.Permission.REMOVE_GUIDE_FROM_EXPEDITION;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.UPDATE_EXPEDITION;
import static com.konstantion.utils.ObjectUtils.anyMatchCollection;
import static com.konstantion.utils.ObjectUtils.requireNonNullOrElseNullable;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@Component
public record ExpeditionServiceImpl(
        ExpeditionValidator tableValidator,
        ExpeditionPort expeditionPort,
        CampPort hallPort,
        EquipmentPort equipmentPort,
        UserPort userPort,
        EquipmentService equipmentService,
        PasswordEncoder passwordEncoder
) implements ExpeditionService {
    private static final Logger logger = LoggerFactory.getLogger(ExpeditionService.class);

    @Override
    public List<Expedition> getAll(boolean onlyActive) {
        List<Expedition> tables = expeditionPort.findAll();
        if (onlyActive) {
            return tables.stream().filter(Expedition::isActive).toList();
        }
        logger.info("Tables successfully returned, {}", tables);
        return tables;
    }

    @Override
    public Expedition addWaiter(UUID tableId, TableWaitersRequest request, User user) {
        if (user.hasNoPermission(ASSIGN_GUIDE_TO_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Expedition table = getByIdOrThrow(tableId);
        ExceptionUtils.isActiveOrThrow(table);

        User waiter = userPort.findById(request.waiterId())
                .orElseThrow(nonExistingIdSupplier(User.class, request.waiterId()));
        ExceptionUtils.isActiveOrThrow(waiter);

        if (table.getGuidesId().add(waiter.getId())) {
            expeditionPort.save(table);
            logger.info("Waiter with id {} successfully added to the table with id {}", waiter.getId(), tableId);
        } else {
            logger.warn("Table with id {} already has waiter with id {}", tableId, waiter.getId());
        }

        return table;
    }

    @Override
    public Expedition removeWaiter(UUID tableId, TableWaitersRequest request, User user) {
        if (user.hasNoPermission(REMOVE_GUIDE_FROM_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Expedition table = getByIdOrThrow(tableId);

        User waiter = userPort.findById(request.waiterId())
                .orElseThrow(nonExistingIdSupplier(User.class, request.waiterId()));

        if (table.getGuidesId().remove(waiter.getId())) {
            expeditionPort.save(table);
            logger.info("Waiter with id {} successfully removed from the table with id {}", waiter.getId(), tableId);
        } else {
            logger.warn("Table with id {} already hasn't waiter with id {}", tableId, waiter.getId());
        }

        return table;
    }

    @Override
    public Expedition update(UUID tableId, UpdateTableRequest request, User user) {
        if (user.hasNoPermission(UPDATE_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        Expedition table = getByIdOrThrow(tableId);

        tableValidator.validate(request).validOrTrow();

        Camp camp = hallPort.findById(request.hallId())
                .orElseThrow(nonExistingIdSupplier(Camp.class, request.hallId()));
        ExceptionUtils.isActiveOrThrow(camp);

        List<Expedition> dbTables = expeditionPort.findAll();

        if (!table.getName().equals(request.name())
                && anyMatchCollection(dbTables, Expedition::getName, request.name(), Objects::equals)) {
            throw new BadRequestException(format("Table with name %s already exist", request.name()));
        }

        if (nonNull(request.password())
                && !passwordEncoder.matches(table.getPassword(), request.password())
                && anyMatchCollection(dbTables, Expedition::getPassword, request.password(), passwordEncoder::matches)) {
            throw new BadRequestException("Table with give password already exist");
        }

        updateTable(table, request);

        expeditionPort.save(table);

        logger.info("Table with id {} successfully updated and returned", tableId);
        return table;
    }


    @Override
    public Expedition activate(UUID tableId, User user) {
        if (user.hasNoPermission(CHANGE_EXPEDITION_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException("Not enough authorities to activate table");
        }

        Expedition table = getByIdOrThrow(tableId);

        if (table.isActive()) {
            logger.warn("Table with id {} is active and returned", tableId);
            return table;
        }

        prepareToActivate(table);
        expeditionPort.save(table);

        logger.info("Table with id {} successfully activated and returned", tableId);
        return table;
    }

    @Override
    public Expedition deactivate(UUID tableId, User user) {
        if (user.hasNoPermission(CHANGE_EXPEDITION_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException("Not enough authorities to deactivate table");
        }

        Expedition table = getByIdOrThrow(tableId);

        canBeDeactivatedOrThrow(table);

        if (!table.isActive()) {
            logger.warn("Table with id {} is inactive and returned", tableId);
            return table;
        }

        prepareToDeactivate(table);
        expeditionPort.save(table);

        logger.info("Table with id {} successfully deactivated and returned", tableId);
        return table;
    }

    @Override
    public Expedition create(CreateTableRequest request, User user) {
        if (user.hasNoPermission(CREATE_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException("Not enough authorities to create table");
        }

        ValidationResult validationResult =
                tableValidator.validate(request);
        validationResult.validOrTrow();

        Camp camp = hallPort.findById(request.hallId())
                .orElseThrow(nonExistingIdSupplier(Camp.class, request.hallId()));
        ExceptionUtils.isActiveOrThrow(camp);

        List<Expedition> dbTables = expeditionPort.findAll();
        if (anyMatchCollection(dbTables, Expedition::getName, request.name(), Objects::equals)) {
            throw new BadRequestException(format("Table with name %s already exist", request.name()));
        }

        if (anyMatchCollection(dbTables, Expedition::getPassword, request.password(), passwordEncoder::matches)) {
            throw new BadRequestException("Table with given password already exist");
        }


        Expedition table = buildTableFromCreateRequestAndHall(request, camp);

        expeditionPort.save(table);

        logger.info("Table successfully created and returned");
        return table;
    }

    @Override
    public Expedition delete(UUID tableId, User user) {
        if (user.hasNoPermission(DELETE_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException("Not enough authorities to delete table");
        }

        Expedition table = getByIdOrThrow(tableId);

        expeditionPort.delete(table);

        logger.info("Table with id {} successfully deleted and returned", tableId);
        return table;
    }

    @Override
    public Expedition getById(UUID tableId) {
        return getByIdOrThrow(tableId);
    }

    @Override
    public Equipment getOrderByTableId(UUID tableId) {
        Expedition table = getByIdOrThrow(tableId);

        if (!table.hasOrder()) {
            return null;
        }

        Equipment equipment = equipmentPort.findById(table.getEquipmentId())
                .orElseThrow(nonExistingIdSupplier(Equipment.class, table.getEquipmentId()));

        logger.info("Order with id {} for table with id {} successfully returned", equipment.getId(), tableId);

        return equipment;
    }

    @Override
    public List<User> getWaitersByTableId(UUID tableId) {
        Expedition table = getByIdOrThrow(tableId);

        List<User> waiters = table.getGuidesId().stream()
                .map(userId ->
                        userPort.findById(userId)
                                .orElseThrow(nonExistingIdSupplier(User.class, userId)))
                .toList();
        logger.info("Waiters for table with id {} successfully returned", tableId);
        return waiters;
    }

    @Override
    public Expedition removeAllWaiters(UUID tableId, User user) {
        if (user.hasNoPermission(ASSIGN_GUIDE_TO_EXPEDITION)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Expedition table = getByIdOrThrow(tableId);
        table.getGuidesId().clear();

        expeditionPort.save(table);
        logger.info("All waiters successfully removed from the table with id {}", tableId);
        return table;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Expedition table = expeditionPort.findByName(username).orElseThrow(() -> {
            throw new UsernameNotFoundException(format("User with username %s doesn't exist", username));
        });
        logger.info("Table with username {} successfully returned", username);
        return table;
    }

    private Expedition getByIdOrThrow(UUID tableId) {
        return expeditionPort.findById(tableId)
                .orElseThrow(nonExistingIdSupplier(Expedition.class, tableId));
    }

    private boolean canBeDeactivatedOrThrow(Expedition table) {
        if (table.hasOrder()) {
            throw new ActiveStateException(format("Table with id %s has active order", table.getId()));
        }
        return true;
    }

    private void prepareToDeactivate(Expedition table) {
        table.setActive(false);
        table.setDeletedAt(now());
        table.getGuidesId().clear();
    }

    private void prepareToActivate(Expedition table) {
        table.setActive(true);
        table.setDeletedAt(null);
    }

    private void updateTable(Expedition table, UpdateTableRequest request) {
        table.setName(requireNonNullOrElseNullable(request.name(), table.getName()));
        table.setCampId(requireNonNullOrElseNullable(request.hallId(), table.getCampId()));
        table.setCapacity(requireNonNullOrElseNullable(request.capacity(), table.getCapacity()));
        if (nonNull(request.password())) {
            table.setPassword(passwordEncoder.encode(request.password()));
        }
        table.setExpeditionType(valueOfTableTypeOrDefault(request.tableType(), table.getExpeditionType()));
    }

    private ExpeditionType valueOfTableTypeOrDefault(String tableType, ExpeditionType orDefault) {
        if (Arrays.stream(ExpeditionType.values()).map(ExpeditionType::name).anyMatch(type -> type.equals(tableType))) {
            return ExpeditionType.valueOf(tableType);
        }
        return orDefault;
    }

    private Expedition buildTableFromCreateRequestAndHall(CreateTableRequest request, Camp camp) {
        return Expedition.builder()
                .name(request.name())
                .capacity(request.capacity())
                .createdAt(now())
                .active(true)
                .guidesId(new HashSet<>())
                .campId(camp.getId())
                .password(passwordEncoder.encode(request.password()))
                .expeditionType(valueOfTableTypeOrDefault(request.tableType(), ExpeditionType.COMMON))
                .build();
    }
}
