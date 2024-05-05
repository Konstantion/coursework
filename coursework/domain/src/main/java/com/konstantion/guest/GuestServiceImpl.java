package com.konstantion.guest;

import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.guest.model.CreateGuestRequest;
import com.konstantion.guest.model.UpdateGuestRequest;
import com.konstantion.guest.validator.GuestValidator;
import com.konstantion.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.user.Permission.CHANGE_GUEST_STATE;
import static com.konstantion.user.Permission.CREATE_GUEST;
import static com.konstantion.user.Permission.DELETE_GUEST;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.UPDATE_GUEST;
import static com.konstantion.utils.ObjectUtils.requireNonNullOrElseNullable;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Service
public record GuestServiceImpl(
        GuestPort guestPort,
        GuestValidator guestValidator
) implements GuestService {
    private static final Logger logger = LoggerFactory.getLogger(GuestServiceImpl.class);

    @Override
    public List<Guest> getAll(boolean onlyActive) {
        List<Guest> guests = guestPort.findAll();
        if (onlyActive) {
            return guests.stream().filter(Guest::isActive).toList();
        }
        logger.info("All guests successfully returned");
        return guests;
    }

    @Override
    public Guest getById(UUID id) {
        Guest guest = getByIdOrThrow(id);
        logger.info("Guest with id {} successfully returned", id);
        return guest;
    }

    @Override
    public Guest create(CreateGuestRequest request, User user) {
        if (user.hasNoPermission(CREATE_GUEST)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        guestValidator.validate(request).validOrTrow();

        guestPort.findByName(request.name()).ifPresent(dbGuest -> {
            throw new BadRequestException(format("Guest with name %s already exist", request.name()));
        });

        Guest guest = Guest.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .discountPercent(request.discountPercent())
                .createdAt(now())
                .active(true)
                .build();

        guestPort.save(guest);
        logger.info("Guest with id {} successfully created and returned", guest.getId());
        return guest;
    }

    @Override
    public Guest update(UUID id, UpdateGuestRequest request, User user) {
        if (user.hasNoPermission(UPDATE_GUEST)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        guestValidator.validate(request).validOrTrow();

        Guest guest = getByIdOrThrow(id);
        ExceptionUtils.isActiveOrThrow(guest);

        if (!guest.getName().equals(request.name())) {
            guestPort.findByName(request.name()).ifPresent(dbGuest -> {
                throw new BadRequestException(format("Guest with name %s already exist", request.name()));
            });
        }

        updateGuest(guest, request);

        guestPort.save(guest);
        logger.info("Guest with id {} successfully updated and returned", guest.getId());
        return guest;
    }

    private void updateGuest(Guest guest, UpdateGuestRequest request) {
        guest.setName(requireNonNullOrElseNullable(request.name(), guest.getName()));
        guest.setPhoneNumber(requireNonNullOrElseNullable(request.phoneNumber(), guest.getPhoneNumber()));
        guest.setDiscountPercent(requireNonNullOrElseNullable(request.discountPercent(), guest.getDiscountPercent()));
    }

    @Override
    public Guest activate(UUID id, User user) {
        if (user.hasNoPermission(CHANGE_GUEST_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        Guest guest = getByIdOrThrow(id);

        if (guest.isActive()) {
            logger.warn("Guest with id {} already active and returned", id);
            return guest;
        }

        prepareToActivate(guest);

        guestPort.save(guest);
        logger.info("Gust with id {} successfully activated", id);
        return guest;
    }

    @Override
    public Guest deactivate(UUID id, User user) {
        if (user.hasNoPermission(CHANGE_GUEST_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        Guest guest = getByIdOrThrow(id);

        if (!guest.isActive()) {
            logger.warn("Guest with id {} already inactive and returned", id);
            return guest;
        }

        prepareToDeactivate(guest);

        guestPort.save(guest);
        logger.info("Gust with id {} successfully deactivated", id);
        return guest;
    }

    @Override
    public Guest delete(UUID id, User user) {
        if (user.hasNoPermission(DELETE_GUEST)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Guest guest = getByIdOrThrow(id);

        guestPort.delete(guest);
        logger.info("Guest with id {} successfully deleted and returned", id);
        return guest;
    }

    private Guest getByIdOrThrow(UUID id) {
        return guestPort.findById(id).orElseThrow(() -> {
            throw new NonExistingIdException(format("Guest with id %s, doesn't exist", id));
        });
    }

    private void prepareToDeactivate(Guest guest) {
        guest.setActive(false);
    }

    private void prepareToActivate(Guest guest) {
        guest.setActive(true);
    }
}
