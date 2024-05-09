package com.konstantion.exception.utils;

import com.konstantion.camp.Camp;
import com.konstantion.equipment.Equipment;
import com.konstantion.exception.ActiveStateException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.expedition.Expedition;
import com.konstantion.gear.Gear;
import com.konstantion.guest.Guest;
import com.konstantion.log.Log;
import com.konstantion.user.User;

import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

public record ExceptionUtils() {
    public static boolean isActiveOrThrow(Expedition table) {
        boolean isActive = table.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Table with id %s, isn't active", table.getId()));
        }
        return true;
    }

    public static boolean isActiveOrThrow(Equipment equipment) {
        boolean isActive = equipment.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Order with id %s, isn't active", equipment.getId()));
        }
        return true;
    }

    public static boolean isActiveOrThrow(Gear gear) {
        boolean isActive = gear.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Product with id %s, isn't active", gear.getId()));
        }
        return true;
    }

    public static boolean isActiveOrThrow(Guest guest) {
        boolean isActive = guest.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Guest with id %s, isn't active", guest.getId()));
        }
        return true;
    }

    public static boolean isActiveOrThrow(Log log) {
        boolean isActive = log.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Bill with id %s, isn't active", log.getId()));
        }
        return true;
    }

    public static boolean isActiveOrThrow(Camp camp) {
        boolean isActive = camp.isActive();
        if (!isActive) {
            throw new ActiveStateException(format("Bill with id %s, isn't active", camp.getId()));
        }
        return true;
    }

    public static <X extends Throwable> Supplier<? extends X> nonExistingIdSupplier(Class<?> target, UUID id) throws X {
        String className = target.getSimpleName();
        return () -> {
            throw new NonExistingIdException(format("%s with id %s, doesn't exist", className, id));
        };
    }

    public static boolean isActiveOrThrow(User user) {
        boolean isActive = user.isEnabled();
        if (!isActive) {
            throw new ActiveStateException(format("User with id %s, isn't active", user.getId()));
        }
        return true;
    }
}
