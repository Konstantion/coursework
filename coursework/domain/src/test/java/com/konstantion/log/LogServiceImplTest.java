package com.konstantion.log;

import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.guest.Guest;
import com.konstantion.guest.GuestPort;
import com.konstantion.log.model.CreateBillRequest;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import com.konstantion.user.UserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class LogServiceImplTest {
    @Mock
    EquipmentPort equipmentPort;
    @Mock
    ExpeditionPort expeditionPort;
    @Mock
    UserPort userPort;
    @Mock
    GuestPort guestPort;
    @Mock
    GearPort gearPort;
    @Mock
    LogPort logPort;
    @Mock
    User user;
    @Mock
    Gear bread;
    @Mock
    Gear water;
    @InjectMocks
    LogServiceImpl billService;
    List<Log> logs;
    Log activeLog;
    Log inactiveLog;
    UUID activeBillId;
    UUID inactiveBillId;
    UUID orderId;
    UUID guestId;
    CreateBillRequest request;

    @BeforeEach
    void setUp() {
        activeBillId = UUID.randomUUID();
        inactiveBillId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        guestId = UUID.randomUUID();

        request = new CreateBillRequest(orderId, guestId);


        activeLog = Log.builder()
                .id(activeBillId)
                .active(true)
                .price(123.0)
                .build();

        inactiveLog = Log.builder()
                .id(inactiveBillId)
                .active(false)
                .price(321.0)
                .build();

        logs = List.of(activeLog, inactiveLog);
    }

    @Test
    void shouldReturnBillsWhenGetBill() {
        when(logPort.findAll()).thenReturn(logs);

        List<Log> activeLogs = billService.getAll();
        List<Log> allLogs = billService.getAll(false);

        assertThat(activeLogs).contains(activeLog);
        assertThat(allLogs).contains(activeLog, inactiveLog);

        verify(logPort, times(2)).findAll();
    }

    @Test
    void shouldReturnBillWhenGetByIdExistingId() {
        when(logPort.findById(activeBillId)).thenReturn(Optional.of(activeLog));

        Log actualLog = billService.getById(activeBillId);

        assertThat(actualLog).isEqualTo(activeLog);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdExistingId() {
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.getById(activeBillId))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCreateWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> billService.create(request, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenCreateWithNonExistingOrder() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.create(request, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateWithInactiveOrder() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(Equipment.builder().active(false).build()));

        assertThatThrownBy(() -> billService.create(request, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateWithOrderThatAlreadyHasBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(Equipment.builder().active(true).logId(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> billService.create(request, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateWithOrderThatDoesNotContainProducts() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(
                Equipment.builder()
                        .active(true)
                        .logId(null)
                        .gearsId(Collections.emptyList())
                        .build()
        ));

        assertThatThrownBy(() -> billService.create(request, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCreateBillWhenCreateWithoutGuestId() {
        request = new CreateBillRequest(UUID.randomUUID(), null);

        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(
                Equipment.builder()
                        .active(true)
                        .logId(null)
                        .gearsId(List.of(UUID.randomUUID(), UUID.randomUUID()))
                        .build()
        ));
        when(gearPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(bread))
                .thenReturn(Optional.of(water));
        when(logPort.save(any(Log.class))).thenReturn(Log.builder().id(UUID.randomUUID()).build());
        when(bread.getPrice()).thenReturn(50.333);
        when(water.getPrice()).thenReturn(50.114);

        Log log = billService.create(request, user);

        assertThat(log.getPrice()).isEqualTo(100.45);
        assertThat(log.getPrice()).isEqualTo(log.getPriceWithDiscount());
    }

    @Test
    void shouldCreateBillWhenCreateWithGuestId() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(
                Equipment.builder()
                        .active(true)
                        .logId(null)
                        .gearsId(List.of(UUID.randomUUID(), UUID.randomUUID()))
                        .build()
        ));
        when(gearPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(bread))
                .thenReturn(Optional.of(water));
        when(logPort.save(any(Log.class))).thenReturn(Log.builder().id(UUID.randomUUID()).build());
        when(guestPort.findById(guestId)).thenReturn(Optional.of(Guest.builder().active(true).discountPercent(10.0).build()));
        when(bread.getPrice()).thenReturn(51.002);
        when(water.getPrice()).thenReturn(49.002);

        Log log = billService.create(request, user);

        assertThat(log.getPrice()).isEqualTo(100.0);
        assertThat(log.getPriceWithDiscount()).isEqualTo(90.0);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCancelWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> billService.cancel(activeBillId, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenCancelWithNonExistingBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.cancel(activeBillId, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenCancelBillOrderDoesNotExist() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.of(Log.builder().active(true).equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.cancel(activeBillId, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldDeleteBillWhenCancelValidBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.of(Log.builder().active(true).equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any(UUID.class))).thenReturn(Optional.of(Equipment.builder().active(true).build()));

        Log log = billService.cancel(activeBillId, user);

        verify(logPort, times(1)).delete(log);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCloseWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> billService.close(activeBillId, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenCloseWithNonExistingBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.cancel(activeBillId, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldDeactivateBillWhenCloseWithValidBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(activeLog))
                .thenReturn(Optional.of(this.inactiveLog));
        when(logPort.save(any(Log.class))).thenReturn(null);

        Log deactivatedLog = billService.close(activeBillId, user);
        Log inactiveLog = billService.close(inactiveBillId, user);

        verify(logPort, times(1)).save(activeLog);
        assertThat(deactivatedLog.isActive()).isFalse();
        assertThat(inactiveLog.isActive()).isFalse();
    }

    @Test
    void shouldThrowForbiddenExceptionWhenActivateWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> billService.activate(activeBillId, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenActivateWithNonExistingBill() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.activate(activeBillId, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldDeactivateBillWhenActivateWithValidBill() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().active(false).build()));
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(logPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(this.activeLog))
                .thenReturn(Optional.of(inactiveLog));
        when(logPort.save(any(Log.class))).thenReturn(null);

        Log activatedLog = billService.activate(inactiveBillId, user);
        Log activeLog = billService.activate(activeBillId, user);

        verify(logPort, times(1)).save(inactiveLog);
        assertThat(activatedLog.isActive()).isTrue();
        assertThat(activeLog.isActive()).isTrue();
    }
}