package com.konstantion.user;

import com.google.common.collect.Sets;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.user.model.CreateUserRequest;
import com.konstantion.user.model.UpdateUserRequest;
import com.konstantion.user.validator.UserValidator;
import com.konstantion.utils.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Role.ADMIN;
import static com.konstantion.user.Role.GUIDE;
import static com.konstantion.user.Role.TABLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @Mock
    UserPort userPort;
    @Mock
    UserValidator userValidator;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    User user;
    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenMethodRequirePermissionWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> userService.createWaiter(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.createAdmin(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.delete(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.activate(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.deactivate(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.removePermission(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.removeRole(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.addPermission(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userService.addRole(null, null, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        when(userPort.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnUserWhenGetByIdExistingId() {
        when(userPort.findById(any(UUID.class))).thenReturn(Optional.of(User.builder().build()));

        User user = userService.getUserById(UUID.randomUUID());

        assertThat(user)
                .isNotNull();
    }

    @Test
    void shouldReturnUsersWhenGetAll() {
        when(userPort.findAll())
                .thenReturn(List.of(
                        User.builder().active(true).roles(Set.of(GUIDE)).build(),
                        User.builder().active(false).roles(Set.of(GUIDE)).build(),
                        User.builder().active(true).roles(Set.of(ADMIN)).build()
                ));

        List<User> activeUsers = userService.getAll();
        List<User> allUsers = userService.getAll(false);
        List<User> allWaiters = userService.getAll(false, GUIDE);

        assertThat(activeUsers)
                .hasSize(2);
        assertThat(allUsers)
                .hasSize(3);
        assertThat(allWaiters)
                .hasSize(2)
                .allMatch(user -> user.getRoles().contains(GUIDE));
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenLoadByUsernameWithNonExistingUsername() {
        when(userPort.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("username"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldReturnUserWhenLoadByUsernameWithExistingUsername() {
        when(userPort.findByEmail(any(String.class))).thenReturn(Optional.of(User.builder().build()));

        UserDetails actual = userService.loadUserByUsername("username");

        assertThat(actual)
                .isNotNull()
                .isExactlyInstanceOf(User.class);
    }

    @Test
    void shouldDeleteUserWhenDelete() {
        when(userPort.findById(any())).thenReturn(Optional.of(User.builder().build()));

        User deleted = userService.delete(UUID.randomUUID(), user);

        assertThat(deleted)
                .isNotNull();
        verify(userPort, times(1)).delete(deleted);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateWaiterOrCreateAdminWithInvalidData() {
        when(userValidator.validate(any(CreateUserRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(userValidator.validateAdmin(any(CreateUserRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        assertThatThrownBy(() -> userService.createWaiter(new CreateUserRequest(null, null, null, null, null, null, null), user))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> userService.createAdmin(new CreateUserRequest(null, null, null, null, null, null, null), user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateWaiterOrCreateAdminWithNameThatAlreadyExist() {
        when(userValidator.validate(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(userValidator.validateAdmin(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").build()));

        assertThatThrownBy(() -> userService.createWaiter(new CreateUserRequest(null, null, "email", null, null, null, null), user))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.createAdmin(new CreateUserRequest(null, null, "email", null, null, null, null), user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateWaiterOrAdminWithPasswordThatAlreadyExist() {
        when(userValidator.validate(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(userValidator.validateAdmin(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").password("password").build()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> userService.createWaiter(new CreateUserRequest(null, null, "newEmail", null, null, "password", null), user))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.createAdmin(new CreateUserRequest(null, null, "newEmail", null, null, "password", null), user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCreateUserWhenCreateWaiterOrAdminWithValidData() {
        when(userValidator.validate(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(userValidator.validateAdmin(any(CreateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").password("password").build()));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        User waiter = userService.createWaiter(new CreateUserRequest("firstName", null, "newEmail", null, null, "password", null), user);
        User admin = userService.createAdmin(new CreateUserRequest(null, "lastName", "newEmail", null, null, "password", null), user);

        assertThat(waiter)
                .isNotNull()
                .matches(user -> user.getFirstName().equals("firstName") && user.getRoles().contains(GUIDE));
        assertThat(admin)
                .isNotNull()
                .matches(user -> user.getLastName().equals("lastName") && user.getRoles().contains(ADMIN));

        verify(userPort, times(2)).save(any(User.class));
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAddOrRemovePermissionSuperUser() {
        UUID randomId = UUID.randomUUID();

        assertThatThrownBy(() -> userService.addPermission(randomId, SUPER_USER, user))
                .isExactlyInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> userService.removePermission(randomId, SUPER_USER, user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldAddPermissionWhenAddPermission() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(true).permissions(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(User.builder().active(true).permissions(Sets.newHashSet(Permission.CALL_GUIDE)).build()));

        User addedPermission = userService.addPermission(randomId, Permission.CALL_GUIDE, user);
        User hadPermission = userService.addPermission(randomId, Permission.CALL_GUIDE, user);

        assertThat(addedPermission.getPermissions())
                .contains(Permission.CALL_GUIDE);
        assertThat(hadPermission.getPermissions())
                .contains(Permission.CALL_GUIDE);

        verify(userPort, times(1)).save(addedPermission);
    }

    @Test
    void shouldRemovePermissionWhenRemovePermission() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(true).permissions(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(User.builder().active(true).permissions(Sets.newHashSet(Permission.CALL_GUIDE)).build()));

        User hadNotPermission = userService.removePermission(randomId, Permission.CALL_GUIDE, user);
        User removedPermission = userService.removePermission(randomId, Permission.CALL_GUIDE, user);

        assertThat(hadNotPermission.getPermissions())
                .doesNotContain(Permission.CALL_GUIDE);
        assertThat(removedPermission.getPermissions())
                .doesNotContain(Permission.CALL_GUIDE);

        verify(userPort, times(1)).save(removedPermission);
    }

    @Test
    void shouldAddRoleWhenAddRole() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(true).roles(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(User.builder().active(true).roles(Sets.newHashSet(GUIDE)).build()));

        User addedRole = userService.addRole(randomId, GUIDE, user);
        User hadRole = userService.addRole(randomId, GUIDE, user);

        assertThat(addedRole.getRoles())
                .contains(GUIDE);
        assertThat(hadRole.getRoles())
                .contains(GUIDE);

        verify(userPort, times(1)).save(addedRole);
    }

    @Test
    void shouldRemoveRoleWhenRemoveRole() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(true).roles(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(User.builder().active(true).roles(Sets.newHashSet(GUIDE)).build()));

        User hadNotRole = userService.removeRole(randomId, GUIDE, user);
        User removedRole = userService.removeRole(randomId, GUIDE, user);

        assertThat(hadNotRole.getPermissions())
                .doesNotContain(Permission.CALL_GUIDE);
        assertThat(removedRole.getPermissions())
                .doesNotContain(Permission.CALL_GUIDE);

        verify(userPort, times(1)).save(removedRole);
    }

    @Test
    void shouldActivateUserWhenActivate() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(false).build()))
                .thenReturn(Optional.of(User.builder().active(true).build()));

        User activatedUser = userService.activate(randomId, user);
        User activeUser = userService.activate(randomId, user);

        assertThat(activatedUser.isEnabled()).isTrue();
        assertThat(activeUser.isEnabled()).isTrue();

        verify(userPort, times(1)).save(activatedUser);
    }

    @Test
    void shouldDeactivateUserWhenDeactivate() {
        UUID randomId = UUID.randomUUID();
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(true).build()))
                .thenReturn(Optional.of(User.builder().active(false).build()));

        User deactivatedUser = userService.deactivate(randomId, user);
        User inactiveUser = userService.deactivate(randomId, user);

        assertThat(deactivatedUser.isEnabled()).isFalse();
        assertThat(inactiveUser.isEnabled()).isFalse();

        verify(userPort, times(1)).save(deactivatedUser);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateWithInvalidData() {
        UUID randomId = UUID.randomUUID();
        when(userValidator.validate(any(UpdateUserRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        assertThatThrownBy(() -> userService.update(randomId, new UpdateUserRequest(null, null, null, null, null, null), user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenUpdateWithNameThatAlreadyExist() {
        UUID randomId = UUID.randomUUID();
        when(userValidator.validate(any(UpdateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").build()));
        when(userPort.findById(randomId)).thenReturn(Optional.of(User.builder().email("different").build()));

        assertThatThrownBy(() -> userService.update(randomId, new UpdateUserRequest(null, null, "email", null, null, null), user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenUpdateWithPasswordThatAlreadyExist() {
        UUID randomId = UUID.randomUUID();
        when(userValidator.validate(any(UpdateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").password("password").build()));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false)
                .thenReturn(true);
        when(userPort.findById(randomId)).thenReturn(Optional.of(User.builder().email("test").password("test").build()));

        assertThatThrownBy(() -> userService.update(randomId, new UpdateUserRequest(null, null, "newEmail", null, null, "password"), user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldUpdateWhenUpdateWithValidData() {
        UUID randomId = UUID.randomUUID();
        when(userValidator.validate(any(UpdateUserRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(userPort.findAll()).thenReturn(List.of(User.builder().email("email").password("password").build()));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false)
                .thenReturn(false);
        when(userPort.findById(randomId)).thenReturn(Optional.of(User.builder().email("test").password("test").build()));

        User updated = userService.update(randomId, new UpdateUserRequest("newFirstName", null, "newEmail", null, null, "password"), user);

        assertThat(updated)
                .isNotNull()
                .matches(user -> user.getEmail().equals("newEmail") && user.getFirstName().equals("newFirstName"));

        verify(userPort, times(1)).save(updated);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAppRoleWithRoleTable() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        assertThatThrownBy(() -> userService.addRole(UUID.randomUUID(), TABLE, user))
                .isInstanceOf(BadRequestException.class);
    }
}