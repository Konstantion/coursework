package com.konstantion.expedition;

import com.google.common.collect.Sets;
import com.konstantion.camp.Camp;
import com.konstantion.camp.CampPort;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.equipment.EquipmentService;
import com.konstantion.exception.ActiveStateException;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.expedition.model.CreateTableRequest;
import com.konstantion.expedition.model.TableWaitersRequest;
import com.konstantion.expedition.model.UpdateTableRequest;
import com.konstantion.expedition.validator.ExpeditionValidator;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import com.konstantion.user.UserPort;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ExpeditionServiceImplTest {
    @Mock
    ExpeditionValidator tableValidator;
    @Mock
    ExpeditionPort expeditionPort;
    @Mock
    CampPort hallPort;
    @Mock
    EquipmentPort equipmentPort;
    @Mock
    UserPort userPort;
    @Mock
    EquipmentService equipmentService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    User user;
    @InjectMocks
    ExpeditionServiceImpl tableService;


    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldReturnAllTabledWhenGetAll() {
        when(expeditionPort.findAll()).thenReturn(List.of(
                Expedition.builder().active(true).build(),
                Expedition.builder().active(false).build()
        ));

        List<Expedition> activeTables = tableService.getAll(true);
        List<Expedition> tables = tableService.getAll(false);

        assertThat(activeTables)
                .hasSize(1);
        assertThat(tables)
                .hasSize(2);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenMethodRequirePermissionWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> tableService.activate(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.deactivate(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.create(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.delete(null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.update(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.addWaiter(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.removeWaiter(null, null, user))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> tableService.removeAllWaiters(null, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        UUID randomId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.getById(randomId))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnTableWhenGetByIdWithExistingId() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().build()));

        Expedition actual = tableService.getById(UUID.randomUUID());

        assertThat(actual).isNotNull();
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenAddWaiterWithNonExistingTableIdOrWaiterId() {
        UUID randomId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(Expedition.builder().active(true).build()));
        when(userPort.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(user.getId()).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> tableService.addWaiter(randomId, null, user))
                .isInstanceOf(NonExistingIdException.class);
        assertThatThrownBy(() -> tableService.addWaiter(existingId, new TableWaitersRequest(randomId), user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowActiveStateExceptionWhenAddWaiterWithNonActiveTableOrWaiter() {
        UUID randomId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Expedition.builder().active(false).build()))
                .thenReturn(Optional.of(Expedition.builder().active(true).build()));
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().active(false).build()));
        when(user.getId()).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> tableService.addWaiter(randomId, null, user))
                .isInstanceOf(ActiveStateException.class);
        assertThatThrownBy(() -> tableService.addWaiter(existingId, new TableWaitersRequest(randomId), user))
                .isInstanceOf(ActiveStateException.class);
    }

    @Test
    void shouldAddWaiterToTableWhenAddWaiterWithValidData() {
        UUID randomId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Expedition.builder().active(true).guidesId(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(Expedition.builder().active(true).guidesId(Sets.newHashSet(randomId)).build()));
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().id(randomId).active(true).build()))
                .thenReturn(Optional.of(User.builder().id(randomId).active(true).build()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        Expedition actualAddedWaiter = tableService.addWaiter(randomId, new TableWaitersRequest(randomId), user);
        Expedition actualHasWaiter = tableService.addWaiter(randomId, new TableWaitersRequest(randomId), user);

        verify(expeditionPort, times(1)).save(actualAddedWaiter);

        assertThat(actualAddedWaiter.getGuidesId()).contains(randomId);
        assertThat(actualHasWaiter.getGuidesId()).contains(randomId);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenRemoveWaiterWithNonExistingTableIdOrWaiterId() {
        UUID randomId = UUID.randomUUID();
        UUID existingId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(Expedition.builder().active(true).build()));
        when(userPort.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(user.getId()).thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> tableService.removeWaiter(randomId, null, user))
                .isInstanceOf(NonExistingIdException.class);
        assertThatThrownBy(() -> tableService.removeWaiter(existingId, new TableWaitersRequest(randomId), user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldRemoveWaiterToTableWhenRemoveWaiterWithValidData() {
        UUID randomId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Expedition.builder().active(true).guidesId(Sets.newHashSet()).build()))
                .thenReturn(Optional.of(Expedition.builder().active(true).guidesId(Sets.newHashSet(randomId)).build()));
        when(userPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(User.builder().id(randomId).active(true).build()))
                .thenReturn(Optional.of(User.builder().id(randomId).active(true).build()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        Expedition actualHasNotWaiter = tableService.removeWaiter(randomId, new TableWaitersRequest(randomId), user);
        Expedition actualRemovedWaiter = tableService.removeWaiter(randomId, new TableWaitersRequest(randomId), user);

        verify(expeditionPort, times(1)).save(actualRemovedWaiter);

        assertThat(actualHasNotWaiter.getGuidesId()).isEmpty();
        assertThat(actualRemovedWaiter.getGuidesId()).isEmpty();
    }

    @Test
    void shouldRemoveAllWaiterWhenRemoveAllWaiters() {
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Expedition.builder().active(true).guidesId(Sets.newHashSet(UUID.randomUUID(), UUID.randomUUID())).build()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        Expedition table = tableService.removeAllWaiters(UUID.randomUUID(), user);

        assertThat(table.getGuidesId()).isEmpty();

        verify(expeditionPort, times(1)).save(table);
    }

    @Test
    void shouldThrowUsernameNotFountExceptionWhenLoadByUsernameWithNonExistingUsername() {
        when(expeditionPort.findByName(any(String.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.loadUserByUsername("username"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldReturnUserDetailsWhenLoadByUsernameWithExistingUsername() {
        String username = "username";
        when(expeditionPort.findByName(username)).thenReturn(Optional.of(Expedition.builder().name(username).build()));

        UserDetails actual = tableService.loadUserByUsername(username);

        assertThat(actual)
                .isNotNull()
                .isInstanceOf(Expedition.class)
                .extracting(UserDetails::getUsername).isEqualTo(username);
    }

    @Test
    void shouldReturnNullWhenGetOrderByTableIdWithTableWithoutOrderId() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().build()));

        Equipment equipment = tableService.getOrderByTableId(UUID.randomUUID());

        assertThat(equipment)
                .isNull();
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetOrderByTableIdWithTableWithNonExistingOrderId() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.getOrderByTableId(UUID.randomUUID()))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnOrderWhenGetOrderByTableIdWithValidData() {
        UUID orderId = UUID.randomUUID();
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().equipmentId(orderId).build()));
        when(equipmentPort.findById(orderId)).thenReturn(Optional.of(Equipment.builder().id(orderId).build()));

        Equipment equipment = tableService.getOrderByTableId(UUID.randomUUID());
        assertThat(equipment)
                .isNotNull()
                .extracting(Equipment::getId).isEqualTo(orderId);
    }

    @Test
    void shouldReturnWaitersWhenGetWaitersByTableId() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().guidesId(Sets.newHashSet(UUID.randomUUID(), UUID.randomUUID())).build()));
        when(userPort.findById(any(UUID.class))).thenReturn(Optional.of(User.builder().id(UUID.randomUUID()).build()));

        List<User> waiters = tableService.getWaitersByTableId(UUID.randomUUID());

        assertThat(waiters)
                .hasSize(2);

        verify(userPort, times(2)).findById(any(UUID.class));
    }

    @Test
    void shouldDeleteTableWhenDeleteTableById() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().build()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        tableService.delete(UUID.randomUUID(), user);

        verify(expeditionPort, times(1)).delete(any(Expedition.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateWithInvalidData() {
        when(tableValidator.validate(any(CreateTableRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);

        assertThatThrownBy(() -> tableService.create(new CreateTableRequest("name", 5, "type", UUID.randomUUID(), "password"), user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowActiveStateExceptionWhenCreateWithInactiveHall() {
        when(tableValidator.validate(any(CreateTableRequest.class))).thenReturn(ValidationResult.valid());
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(hallPort.findById(any(UUID.class))).thenReturn(Optional.of(Camp.builder().active(false).build()));

        assertThatThrownBy(() -> tableService.create(new CreateTableRequest("name", 5, "type", UUID.randomUUID(), "password"), user))
                .isInstanceOf(ActiveStateException.class);
    }

    @Test
    void shouldCreateTableWhenCreateWithValidData() {
        UUID hallId = UUID.randomUUID();
        CreateTableRequest createTableRequest = new CreateTableRequest("name", 5, "type", hallId, "password");
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(tableValidator.validate(any(CreateTableRequest.class))).thenReturn(ValidationResult.valid());
        when(hallPort.findById(any(UUID.class))).thenReturn(Optional.of(Camp.builder().id(hallId).active(true).build()));
        when(expeditionPort.findAll()).thenReturn(List.of());

        Expedition actual = tableService.create(createTableRequest, user);

        assertThat(actual)
                .isNotNull()
                .extracting(Expedition::getCampId).isEqualTo(hallId);

    }

    @Test
    void shouldThrowBadRequestWhenCreateWithNameAndPasswordThatAlreadyExist() {
        UUID hallId = UUID.randomUUID();
        CreateTableRequest createTableRequest = new CreateTableRequest("name", 5, "type", hallId, "password");
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(tableValidator.validate(any(CreateTableRequest.class))).thenReturn(ValidationResult.valid());
        when(hallPort.findById(any(UUID.class))).thenReturn(Optional.of(Camp.builder().id(hallId).active(true).build()));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(expeditionPort.findAll())
                .thenReturn(List.of(Expedition.builder().name("name").build()))
                .thenReturn(List.of(Expedition.builder().password("password").build()));

        assertThatThrownBy(() -> tableService.create(createTableRequest, user))
                .isExactlyInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> tableService.create(createTableRequest, user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateWithNameAndPasswordThatAlreadyExist() {
        UUID hallId = UUID.randomUUID();
        UpdateTableRequest updateTableRequest = new UpdateTableRequest("name", 5, "type", hallId, "password");
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(tableValidator.validate(any(UpdateTableRequest.class))).thenReturn(ValidationResult.valid());
        when(hallPort.findById(any(UUID.class))).thenReturn(Optional.of(Camp.builder().id(hallId).active(true).build()));
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().name("table").password("1111").build()));
        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false)
                .thenReturn(true);
        when(expeditionPort.findAll())
                .thenReturn(List.of(Expedition.builder().name("name").build()))
                .thenReturn(List.of(Expedition.builder().password("password").build()));

        assertThatThrownBy(() -> tableService.update(UUID.randomUUID(), updateTableRequest, user))
                .isExactlyInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> tableService.update(UUID.randomUUID(), updateTableRequest, user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateWithInvalidData() {
        when(tableValidator.validate(any(UpdateTableRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().name("table").password("1111").build()));

        assertThatThrownBy(() -> tableService.update(UUID.randomUUID(), new UpdateTableRequest("name", 5, "type", UUID.randomUUID(), "password"), user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldUpdateTableWhenUpdateWithValidData() {
        UUID hallId = UUID.randomUUID();
        Expedition table = Expedition.builder().name("name").password("password").build();
        UpdateTableRequest updateTableRequest = new UpdateTableRequest("name", 5, ExpeditionType.VIP.name(), hallId, "password");
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(tableValidator.validate(any(UpdateTableRequest.class))).thenReturn(ValidationResult.valid());
        when(hallPort.findById(any(UUID.class))).thenReturn(Optional.of(Camp.builder().id(hallId).active(true).build()));
        when(expeditionPort.findAll()).thenReturn(List.of(table));
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(table));

        Expedition actual = tableService.update(UUID.randomUUID(), updateTableRequest, user);

        assertThat(actual)
                .isNotNull()
                .extracting(Expedition::getExpeditionType).isEqualTo(ExpeditionType.VIP);
        assertThat(actual)
                .extracting(Expedition::getCapacity).isEqualTo(5);
    }

    @Test
    void shouldActivateTableWhenActivate() {
        Expedition table = Expedition.builder().name("name").active(false).password("password").build();
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(table))
                .thenReturn(Optional.of(Expedition.builder().active(true).build()));

        Expedition actualActivated = tableService.activate(UUID.randomUUID(), user);
        Expedition actualActive = tableService.activate(UUID.randomUUID(), user);

        assertThat(actualActivated)
                .isNotNull()
                .extracting(Expedition::isActive).isEqualTo(true);
        assertThat(actualActive)
                .isNotNull()
                .extracting(Expedition::isActive).isEqualTo(true);

        verify(expeditionPort, times(1)).save(actualActivated);
    }

    @Test
    void shouldDeactivateTableWhenDeactivate() {
        Expedition table = Expedition.builder().name("name").active(true).password("password").build();
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(expeditionPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(table))
                .thenReturn(Optional.of(Expedition.builder().active(false).build()));

        Expedition actualDeactivated = tableService.deactivate(UUID.randomUUID(), user);
        Expedition actualInactive = tableService.deactivate(UUID.randomUUID(), user);

        assertThat(actualDeactivated)
                .isNotNull()
                .extracting(Expedition::isActive).isEqualTo(false);
        assertThat(actualInactive)
                .isNotNull()
                .extracting(Expedition::isActive).isEqualTo(false);

        verify(expeditionPort, times(1)).save(actualDeactivated);
    }
}