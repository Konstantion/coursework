package com.konstantion.camp;

import com.konstantion.camp.model.CreateHallRequest;
import com.konstantion.camp.model.UpdateHallRequest;
import com.konstantion.camp.validator.CampValidator;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CHANGE_CAMP_STATE;
import static com.konstantion.user.Permission.CREATE_CAMP;
import static com.konstantion.user.Permission.DELETE_CAMP;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.UPDATE_CAMP;
import static com.konstantion.utils.ObjectUtils.requireNonNullOrElseNullable;
import static java.time.LocalDateTime.now;

@Component
public record CampServiceImpl(
        CampPort hallPort,
        ExpeditionPort expeditionPort,
        CampValidator hallValidator
) implements CampService {
    private static final Logger logger = LoggerFactory.getLogger(CampServiceImpl.class);

    @Override
    public Camp create(CreateHallRequest request, User user) {
        if (user.hasNoPermission(CREATE_CAMP)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        hallValidator.validate(request).validOrTrow();

        Camp camp = Camp.builder()
                .name(request.name())
                .active(true)
                .createdAt(now())
                .build();

        hallPort.save(camp);
        logger.info("Camp with id {} successfully created and returned", camp.getId());
        return camp;
    }

    @Override
    public Camp getById(UUID id) {
        Camp camp = getByIdOrThrow(id);
        logger.info("Camp with id {} successfully returned", camp.getId());
        return camp;
    }

    @Override
    public List<Camp> getAll(boolean onlyActive) {
        List<Camp> camps = hallPort.findAll();
        if (onlyActive) {
            return camps.stream().filter(Camp::isActive).toList();
        }
        logger.info("All camps successfully returned");
        return camps;
    }

    @Override
    public Camp update(UUID id, UpdateHallRequest request, User user) {
        if (user.hasNoPermission(UPDATE_CAMP)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        hallValidator.validate(request).validOrTrow();

        Camp camp = getByIdOrThrow(id);

        updateHall(camp, request);

        hallPort.save(camp);
        logger.info("Camp with id {} successfully updated and returned", id);
        return camp;
    }

    @Override
    public Camp activate(UUID id, User user) {
        if (user.hasNoPermission(CHANGE_CAMP_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Camp camp = getByIdOrThrow(id);

        if (camp.isActive()) {
            logger.warn("Hall with id {} is already active and returned", id);
            return camp;
        }

        prepareToActivate(camp);

        hallPort.save(camp);
        logger.info("Camp with id {} successfully activated and returned", id);
        return camp;
    }

    @Override
    public Camp deactivate(UUID id, User user) {
        if (user.hasNoPermission(CHANGE_CAMP_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Camp camp = getByIdOrThrow(id);

        if (!camp.isActive()) {
            logger.warn("Camp with id {} is already inactive and returned", id);
            return camp;
        }

        prepareToDeactivate(camp);

        hallPort.save(camp);
        logger.info("Camp with id {} successfully deactivated and returned", id);
        return camp;
    }

    @Override
    public Camp delete(UUID id, User user) {
        if (user.hasNoPermission(DELETE_CAMP)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        Camp camp = getByIdOrThrow(id);

        hallPort.delete(camp);
        logger.info("Camp with id {} successfully deleted", id);
        return camp;
    }

    @Override
    public List<Expedition> getTablesByHallId(UUID id, boolean onlyActive) {
        getByIdOrThrow(id);

        List<Expedition> tables = expeditionPort.findAllWhereHallId(id);
        if (onlyActive) {
            tables = tables.stream().filter(Expedition::isActive).toList();
        }
        logger.info("Expeditions in camp with id {} successfully returned", id);
        return tables;
    }

    private Camp getByIdOrThrow(UUID id) {
        return hallPort.findById(id)
                .orElseThrow(nonExistingIdSupplier(Camp.class, id));
    }

    private void prepareToActivate(Camp camp) {
        camp.setActive(true);
    }

    private void prepareToDeactivate(Camp camp) {
        camp.setActive(false);
    }

    private void updateHall(Camp camp, UpdateHallRequest request) {
        camp.setName(requireNonNullOrElseNullable(request.name(), camp.getName()));
    }
}
