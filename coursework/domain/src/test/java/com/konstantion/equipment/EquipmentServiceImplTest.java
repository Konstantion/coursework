package com.konstantion.equipment;

import com.google.common.collect.Lists;
import com.konstantion.equipment.model.OrderProductsRequest;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.log.Log;
import com.konstantion.log.LogPort;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EquipmentServiceImplTest {
    @Mock
    ExpeditionPort expeditionPort;
    @Mock
    GearPort gearPort;
    @Mock
    EquipmentPort equipmentPort;
    @Mock
    LogPort logPort;
    @Mock
    User user;
    @InjectMocks
    EquipmentServiceImpl orderService;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
    }

    @Test
    void shouldReturnAllOrdersWhenGetAll() {
        when(equipmentPort.findAll()).thenReturn(List.of(
                Equipment.builder().active(true).build(),
                Equipment.builder().active(false).build()
        ));

        List<Equipment> activeEquipments = orderService.getAll(true);
        List<Equipment> allEquipments = orderService.getAll(false);

        assertThat(activeEquipments)
                .hasSize(1);
        assertThat(allEquipments)
                .hasSize(2);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        when(equipmentPort.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(UUID.randomUUID()))
                .isExactlyInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnOrderWhenGetByIdWithExistingId() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().build()));

        Equipment actual = orderService.getById(UUID.randomUUID());

        assertThat(actual).isNotNull();
    }

    @Test
    void shouldThrowForbiddenExceptionWhenMethodRequirePermissionWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.close(null, user))
                .isInstanceOf(ForbiddenException.class);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.delete(null, user))
                .isInstanceOf(ForbiddenException.class);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.open(null, user))
                .isInstanceOf(ForbiddenException.class);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.transferToAnotherTable(null, null, user))
                .isInstanceOf(ForbiddenException.class);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.addProduct(null, null, user))
                .isInstanceOf(ForbiddenException.class);

        AssertionsForClassTypes.assertThatThrownBy(() -> orderService.removeProduct(null, null, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenTransferToAnotherTableWithTableThatHasOrder() {
        when(expeditionPort.findById(any())).thenReturn(Optional.of(Expedition.builder().active(true).equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().build()));

        assertThatThrownBy(() -> orderService.transferToAnotherTable(UUID.randomUUID(), UUID.randomUUID(), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldTransferOrderWhenTransferToAnotherTableWithTableThatHasOrder() {
        UUID orderId = UUID.randomUUID();
        UUID tableId = UUID.randomUUID();
        Expedition dbTable = Expedition.builder().id(tableId).active(true).equipmentId(null).build();
        when(expeditionPort.findById(tableId)).thenReturn(Optional.of(dbTable));
        when(equipmentPort.findById(orderId)).thenReturn(Optional.of(Equipment.builder().id(orderId).build()));

        Equipment equipment = orderService.transferToAnotherTable(orderId, tableId, user);

        assertThat(equipment)
                .isNotNull()
                .extracting(Equipment::getExpeditionId)
                .isEqualTo(tableId);

        verify(expeditionPort, times(1)).save(dbTable);
    }

    @Test
    void shouldTransferOrderAndClearOldTableWhenTransferToAnotherTableWithTableThatHasOrder() {
        UUID orderId = UUID.randomUUID();
        UUID newTableId = UUID.randomUUID();
        UUID oldTableId = UUID.randomUUID();

        Expedition dbTable = Expedition.builder().id(newTableId).active(true).equipmentId(null).build();
        when(expeditionPort.findById(newTableId)).thenReturn(Optional.of(dbTable));
        when(expeditionPort.findById(oldTableId)).thenReturn(Optional.of(Expedition.builder().build()));
        when(equipmentPort.findById(orderId)).thenReturn(Optional.of(Equipment.builder().id(orderId).expeditionId(oldTableId).build()));

        Equipment equipment = orderService.transferToAnotherTable(orderId, newTableId, user);

        assertThat(equipment)
                .isNotNull()
                .extracting(Equipment::getExpeditionId)
                .isEqualTo(newTableId);

        verify(expeditionPort, times(2)).save(any());
    }

    @Test
    void shouldThrowBadRequestExceptionOrderWithTableThatHasOrder() {
        when(expeditionPort.findById(any())).thenReturn(Optional.of(Expedition.builder().active(true).equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().build()));

        assertThatThrownBy(() -> orderService.open(UUID.randomUUID(), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCreateOrderWhenOpen() {
        UUID tableId = UUID.randomUUID();
        Expedition dbTable = Expedition.builder().id(tableId).active(true).equipmentId(null).build();
        when(expeditionPort.findById(tableId)).thenReturn(Optional.of(dbTable));

        Equipment equipment = orderService.open(tableId, user);

        assertThat(equipment).isNotNull()
                .extracting(Equipment::getExpeditionId)
                .isEqualTo(tableId);
        assertThat(equipment.getCreatedAt())
                .isEqualToIgnoringHours(now());
        assertThat(equipment.isActive()).isTrue();

        verify(expeditionPort, times(1)).save(dbTable);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCloseWithOrderWithoutBill() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(
                Equipment.builder()
                        .gearsId(List.of(UUID.randomUUID()))
                        .logId(null)
                        .active(true)
                        .build()
        ));

        assertThatThrownBy(() -> orderService.close(UUID.randomUUID(), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCloseWithOrderWithActiveBill() {
        when(logPort.findById(any())).thenReturn(Optional.of(Log.builder().active(true).build()));
        when(expeditionPort.findById(any())).thenReturn(Optional.of(Expedition.builder().equipmentId(UUID.randomUUID()).build()));
        when(equipmentPort.findById(any())).thenReturn(Optional.of(
                Equipment.builder()
                        .gearsId(List.of(UUID.randomUUID()))
                        .logId(UUID.randomUUID())
                        .expeditionId(UUID.randomUUID())
                        .active(true)
                        .build()
        ));

        assertThatThrownBy(() -> orderService.close(UUID.randomUUID(), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCloseWhenCloseWithValidData() {
        UUID tableId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(logPort.findById(any())).thenReturn(Optional.of(Log.builder().active(false).build()));
        when(expeditionPort.findById(any())).thenReturn(Optional.of(Expedition.builder().id(tableId).equipmentId(orderId).build()));
        when(equipmentPort.findById(any())).thenReturn(Optional.of(
                Equipment.builder()
                        .id(orderId)
                        .gearsId(List.of(UUID.randomUUID()))
                        .logId(UUID.randomUUID())
                        .expeditionId(tableId)
                        .active(true)
                        .build()
        ));

        Equipment closed = orderService.close(UUID.randomUUID(), user);

        assertThat(closed).isNotNull()
                .extracting(Equipment::isActive)
                .isEqualTo(false);
        assertThat(closed.getClosedAt()).isNotNull()
                .isEqualToIgnoringHours(now());

        verify(expeditionPort, times(1)).save(any());
        verify(equipmentPort, times(1)).save(any());
    }

    @Test
    void shouldDeleteOrderWhenDelete() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().build()));

        Equipment deleted = orderService.delete(UUID.randomUUID(), user);

        assertThat(deleted).isNotNull();
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAddProductOrRemoveProductWithInvalidQuantity() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().active(true).gearsId(List.of()).build()));
        when(gearPort.findById(any())).thenReturn(Optional.of(Gear.builder().active(true).build()));
        assertThatThrownBy(() -> orderService.addProduct(UUID.randomUUID(), new OrderProductsRequest(UUID.randomUUID(), -1), user))
                .isExactlyInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> orderService.removeProduct(UUID.randomUUID(), new OrderProductsRequest(UUID.randomUUID(), -1), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldAddProductsWhenAddProductWithValidQuantity() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().active(true).gearsId(Lists.newArrayList()).build()));
        when(gearPort.findById(any())).thenReturn(Optional.of(Gear.builder().active(true).build()));
        int counter = orderService.addProduct(UUID.randomUUID(), new OrderProductsRequest(UUID.randomUUID(), 3), user);

        assertThat(counter).isEqualTo(3);

        verify(equipmentPort, times(1)).save(any());
        verify(gearPort, times(1)).findById(any());
    }

    @Test
    void shouldRemoveProductsWhenRemoveProductsWithValidQuantity() {
        UUID productId = UUID.randomUUID();
        Gear gear = Gear.builder().id(productId).active(true).build();
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().active(true).gearsId(Lists.newArrayList(productId, productId, productId)).build()));
        when(gearPort.findById(productId)).thenReturn(Optional.of(gear));

        int counter = orderService.removeProduct(UUID.randomUUID(), new OrderProductsRequest(productId, 10), user);

        assertThat(counter).isEqualTo(3);

        verify(equipmentPort, times(1)).save(any());
        verify(gearPort, times(1)).findById(any());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenAddProductOrRemoveProductWithOrderThatHasBill() {
        when(equipmentPort.findById(any())).thenReturn(Optional.of(Equipment.builder().active(true).logId(UUID.randomUUID()).gearsId(List.of()).build()));
        when(gearPort.findById(any())).thenReturn(Optional.of(Gear.builder().active(true).build()));

        assertThatThrownBy(() -> orderService.addProduct(UUID.randomUUID(), new OrderProductsRequest(UUID.randomUUID(), 1), user))
                .isExactlyInstanceOf(BadRequestException.class);

        assertThatThrownBy(() -> orderService.removeProduct(UUID.randomUUID(), new OrderProductsRequest(UUID.randomUUID(), 1), user))
                .isExactlyInstanceOf(BadRequestException.class);
    }
}