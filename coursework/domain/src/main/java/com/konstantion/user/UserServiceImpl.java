package com.konstantion.user;

import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.utils.ExceptionUtils;
import com.konstantion.user.model.CreateUserRequest;
import com.konstantion.user.model.UpdateUserRequest;
import com.konstantion.user.validator.UserValidator;
import com.konstantion.utils.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CHANGE_USER_STATE;
import static com.konstantion.user.Permission.CREATE_USER;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.getDefaultAdminPermission;
import static com.konstantion.user.Permission.getDefaultWaiterPermission;
import static com.konstantion.user.Role.ADMIN;
import static com.konstantion.user.Role.getAdminRole;
import static com.konstantion.user.Role.getWaiterRole;
import static com.konstantion.utils.ObjectUtils.anyMatchCollection;
import static com.konstantion.utils.ObjectUtils.requireNonNullOrElseNullable;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@Component
public record UserServiceImpl(
        UserPort userPort,
        UserValidator userValidator,
        PasswordEncoder passwordEncoder
) implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    @Override
    public List<User> getAll(boolean onlyActive, Role role) {
        List<User> users = userPort.findAll();
        if (onlyActive) {
            users = users.stream().filter(User::getActive).toList();
        }
        if (nonNull(role)) {
            users = users.stream().filter(user -> user.getRoles().contains(role)).toList();
        }
        logger.info("All users with role {} successfully returned", role);
        return users;
    }

    @Override
    public User getUserById(UUID id) {
        User user = getByIdOrThrow(id);
        logger.info("User with id {} successfully returned", id);
        return user;
    }

    @Override
    public User createWaiter(CreateUserRequest request, User authorized) {
        if (authorized.hasNoPermission(CREATE_USER)
                && authorized.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        ValidationResult validationResult = userValidator.validate(request);
        validationResult.validOrTrow();

        List<User> dbUsers = userPort.findAll();

        if (anyMatchCollection(dbUsers, User::getEmail, request.email(), Objects::equals)) {
            throw new BadRequestException(format("User with email %s already exist", request.email()));
        }

        if (anyMatchCollection(dbUsers, User::getPassword, request.password(), passwordEncoder::matches)) {
            throw new BadRequestException("User with given password already exist");
        }

        User waiter = buildWaiterFromRequest(request);

        userPort.save(waiter);

        logger.info("Waiter successfully created");
        return waiter;
    }

    @Override
    public User createAdmin(CreateUserRequest request, User user) {
        if (user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        ValidationResult validationResult = userValidator.validateAdmin(request);
        validationResult.validOrTrow();

        List<User> dbUsers = userPort.findAll();

        if (anyMatchCollection(dbUsers, User::getEmail, request.email(), Objects::equals)) {
            throw new BadRequestException(format("User with email %s already exist", request.email()));
        }

        if (anyMatchCollection(dbUsers, User::getPassword, request.password(), passwordEncoder::matches)) {
            throw new BadRequestException("User with given password already exist");
        }

        User admin = buildAdminFromRequest(request);

        userPort.save(admin);

        logger.info("Admin successfully created");

        return admin;
    }

    @Override
    public User addPermission(UUID userId, Permission permission, User authenticated) {
        if (authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (permission.equals(SUPER_USER)) {
            throw new BadRequestException("Permission SUPER USER can't be either removed or added");
        }

        User user = getByIdOrThrow(userId);
        ExceptionUtils.isActiveOrThrow(user);

        if (!user.getPermissions().add(permission)) {
            logger.warn("User with id {} already has permission {}", userId, permission);
            return user;
        }

        userPort.save(user);

        logger.info("Permission {} successfully added to the user with id {}", permission, userId);

        return user;
    }

    @Override
    public User removePermission(UUID userId, Permission permission, User authenticated) {
        if (authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (permission.equals(SUPER_USER)) {
            throw new BadRequestException("Permission SUPER USER can't be either removed or added");
        }

        User user = getByIdOrThrow(userId);
        ExceptionUtils.isActiveOrThrow(user);

        if (!user.getPermissions().remove(permission)) {
            logger.warn("User with id {} doesn't have permission {}", userId, permission);
            return user;
        }

        userPort.save(user);

        logger.info("Permission {} successfully removed from the user with id {}", permission, userId);

        return user;
    }

    @Override
    public User addRole(UUID userId, Role role, User authenticated) {
        if (authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (role.equals(Role.TABLE)) {
            throw new BadRequestException("Role TABLE can't be added");
        }

        User user = getByIdOrThrow(userId);
        ExceptionUtils.isActiveOrThrow(user);

        if (!user.getRoles().add(role)) {
            logger.warn("User with id {} already has role {}", userId, role);
            return user;
        }

        userPort.save(user);

        logger.info("Role {} successfully added to the user with id {}", role, userId);

        return user;
    }

    @Override
    public User removeRole(UUID userId, Role role, User authenticated) {
        if (authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (userId.equals(authenticated.getId()) && role.equals(ADMIN)) {
            throw new BadRequestException("Outstanding Move! And how would you manage the application without this role :)");
        }

        User user = getByIdOrThrow(userId);
        ExceptionUtils.isActiveOrThrow(user);

        if (!user.getRoles().remove(role)) {
            logger.warn("User with id {} doesn't have role {}", userId, role);
            return user;
        }

        userPort.save(user);

        logger.info("Role {} successfully removed from the user with id {}", role, userId);

        return user;
    }

    @Override
    public User update(UUID userId, UpdateUserRequest request, User authorized) {
        if (authorized.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        userValidator.validate(request).validOrTrow();

        User user = getUserById(userId);

        List<User> dbUsers = userPort.findAll();

        if (!user.getEmail().equals(request.email())
                && anyMatchCollection(dbUsers, User::getEmail, request.email(), Objects::equals)) {
            throw new BadRequestException(format("User with email %s already exist", request.email()));
        }

        if (nonNull(request.password())
                && !passwordEncoder.matches(user.getPassword(), request.password())
                && anyMatchCollection(dbUsers, User::getPassword, request.password(), passwordEncoder::matches)) {
            throw new BadRequestException("User with given password already exist");
        }

        updateUser(user, request);

        userPort.save(user);

        logger.info("User with id {} successfully updated", userId);

        return user;
    }

    @Override
    public User deactivate(UUID userId, User authenticated) {
        if (authenticated.hasNoPermission(CHANGE_USER_STATE)
                && authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (userId.equals(authenticated.getId())) {
            throw new BadRequestException("Deactivating yourself is not allowed :)");
        }

        User user = getUserById(userId);

        if (user.hasPermission(SUPER_USER)) {
            throw new BadRequestException("Deactivating  SUPER USER is not allowed");
        }

        if (!user.isEnabled()) {
            logger.warn("User with id {} is already inactive", userId);
            return user;
        }

        user.setActive(false);
        userPort.save(user);

        logger.info("User with id {} successfully deactivated", userId);
        return user;
    }

    @Override
    public User activate(UUID userId, User authenticated) {
        if (authenticated.hasNoPermission(CHANGE_USER_STATE)
                && authenticated.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (userId.equals(authenticated.getId())) {
            throw new BadRequestException("Activating yourself is not allowed :)");
        }

        User user = getUserById(userId);

        if (user.isEnabled()) {
            logger.warn("User with id {} is already active", userId);
            return user;
        }

        user.setActive(true);
        userPort.save(user);

        logger.info("User with id {} successfully activated", userId);
        return user;
    }

    @Override
    public User delete(UUID userId, User authorized) {
        if (authorized.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        if (userId.equals(authorized.getId())) {
            throw new BadRequestException("Chill body suicide is not an option :(");
        }

        User user = getUserById(userId);

        if (user.hasPermission(SUPER_USER)) {
            throw new BadRequestException("Deleting SUPER USER is not allowed");
        }

        userPort.delete(user);

        logger.info("User with id {} successfully deleted", userId);

        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userPort.findByEmail(username).orElseThrow(() -> {
                    throw new UsernameNotFoundException(format("User with username %s doesn't exist", username));
                }
        );
    }

    private User getByIdOrThrow(UUID uuid) {
        return userPort.findById(uuid)
                .orElseThrow(nonExistingIdSupplier(User.class, uuid));
    }

    private void updateUser(User user, UpdateUserRequest request) {
        user.setEmail(requireNonNullOrElseNullable(request.email(), user.getEmail()));
        user.setFirstName(requireNonNullOrElseNullable(request.firstName(), user.getFirstName()));
        user.setLastName(requireNonNullOrElseNullable(request.lastName(), user.getLastName()));
        user.setPhoneNumber(requireNonNullOrElseNullable(request.phoneNumber(), user.getPhoneNumber()));
        user.setAge(requireNonNullOrElseNullable(request.age(), user.getAge()));
        if (nonNull(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
    }

    private User buildUserFromCreateRequest(CreateUserRequest request) {
        return User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .age(request.age())
                .password(passwordEncoder.encode(request.password()))
                .active(true)
                .createdAt(now())
                .build();
    }

    private User buildWaiterFromRequest(CreateUserRequest request) {
        User user = buildUserFromCreateRequest(request);
        user.setRoles(getWaiterRole());
        user.setPermissions(getDefaultWaiterPermission());

        return user;
    }

    private User buildAdminFromRequest(CreateUserRequest request) {
        User user = buildUserFromCreateRequest(request);
        user.setRoles(getAdminRole());
        user.setPermissions(getDefaultAdminPermission());

        return user;
    }
}
