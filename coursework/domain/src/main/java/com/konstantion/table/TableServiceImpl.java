package com.konstantion.table;

import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.ValidationException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.hall.Hall;
import com.konstantion.hall.HallPort;
import com.konstantion.order.OrderService;
import com.konstantion.table.model.CreateTableRequest;
import com.konstantion.table.validator.TableValidator;
import com.konstantion.user.User;
import com.konstantion.user.UserService;
import com.konstantion.utils.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CREATE_TABLE;
import static com.konstantion.user.Permission.DELETE_TABLE;
import static com.konstantion.user.Role.ADMIN;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Component
public record TableServiceImpl(
        TableValidator tableValidator,
        TablePort tableRepository,
        HallPort hallPort,
        UserService userService,
        OrderService orderService
) implements TableService {
    private static final Logger logger = LoggerFactory.getLogger(TableService.class);
    @Override
    public Table activate(UUID tableId, User user) {
        if (user.hasNoPermission(ADMIN)) {
            throw new ForbiddenException("Not enough authorities to activate table");
        }

        Table table = getByIdOrThrow(tableId);

        if (table.isActive()) {
            return table;
        }

        prepareToActivate(table);
        tableRepository.save(table);

        return table;
    }

    @Override
    public Table deactivate(UUID tableId, User user) {
        if (user.hasNoPermission(ADMIN)) {
            throw new ForbiddenException("Not enough authorities to deactivate table");
        }

        Table table = getByIdOrThrow(tableId);

        canBeDeactivatedOrThrow(table);

        if (!table.isActive()) {
            return table;
        }

        prepareToDeactivate(table);
        tableRepository.save(table);

        return table;
    }

    @Override
    public Table create(CreateTableRequest createTableRequest, User user) {
        if (user.hasNoPermission(CREATE_TABLE)) {
            throw new ForbiddenException("Not enough authorities to create table");
        }

        ValidationResult validationResult =
                tableValidator.validate(createTableRequest);

        if (validationResult.errorsPresent()) {
            throw new ValidationException("Validation error, table is invalid", validationResult.errorsMap());
        }

        Hall hall = hallPort.findById(createTableRequest.hallUuid())
                .orElseThrow(nonExistingIdSupplier(Hall.class, createTableRequest.hallUuid()));
        ExceptionUtils.isActiveOrThrow(hall);

        Table table = Table.builder()
                .name(createTableRequest.name())
                .capacity(createTableRequest.capacity())
                .createdAt(now())
                .active(true)
                .waitersId(new ArrayList<>())
                .hallId(hall.getId())
                .tableType(TableType.valueOf(createTableRequest.tableType()))
                .build();

        tableRepository.save(table);

        logger.info("Table {} successfully created", table);

        return table;
    }

    @Override
    public Table delete(UUID tableId, User user) {
        if (user.hasNoPermission(ADMIN, DELETE_TABLE)) {
            throw new ForbiddenException("Not enough authorities to delete table");
        }

        Table table = getByIdOrThrow(tableId);

        tableRepository.delete(table);

        return table;
    }

    @Override
    public Table getById(UUID tableId, User user) {
        return getByIdOrThrow(tableId);
    }


    private Table getByIdOrThrow(UUID tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(nonExistingIdSupplier(Table.class, tableId));
    }

    private boolean canBeDeactivatedOrThrow(Table table) {
        if (table.hasOrder()) {
            throw new BadRequestException(format("Table with id %s has active order", table.getId()));
        }
        return true;
    }

    private void prepareToDeactivate(Table table) {
        table.setActive(false);
        table.setDeletedAt(now());
        table.getWaitersId().clear();
    }

    private void prepareToActivate(Table table) {
        table.setActive(true);
        table.setDeletedAt(null);
    }

}