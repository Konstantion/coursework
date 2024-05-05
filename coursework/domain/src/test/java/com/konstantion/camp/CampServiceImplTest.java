package com.konstantion.camp;

import com.github.javafaker.Faker;
import com.konstantion.camp.model.CreateHallRequest;
import com.konstantion.camp.model.UpdateHallRequest;
import com.konstantion.camp.validator.CampValidator;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import com.konstantion.utils.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CampServiceImplTest {
    @Mock
    CampPort hallPort;
    @Mock
    ExpeditionPort expeditionPort;
    @Mock
    CampValidator hallValidator;
    @InjectMocks
    CampServiceImpl hallService;
    @Mock
    User user;
    Faker faker;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        faker = new Faker();
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCallMethodWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> hallService.create(null, user)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> hallService.deactivate(null, user)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> hallService.delete(null, user)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> hallService.activate(null, user)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> hallService.update(null, null, user)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        when(hallPort.findById(any())).thenReturn(Optional.empty());
        UUID randomUUID = UUID.randomUUID();

        assertThatThrownBy(() -> hallService.getById(randomUUID))
                .isExactlyInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnHallWhenGetByIdWithExistingId() {
        when(hallPort.findById(any())).thenReturn(Optional.of(Camp.builder().build()));

        Camp actual = hallService.getById(UUID.randomUUID());

        assertThat(actual).isNotNull();
    }

    @Test
    void shouldReturnHallWhenGetAll() {
        when(hallPort.findAll()).thenReturn(List.of(
                Camp.builder().active(true).build(),
                Camp.builder().active(false).build()
        ));

        List<Camp> actualActive = hallService.getAll(true);
        List<Camp> actualAll = hallService.getAll(false);

        assertThat(actualActive)
                .hasSize(1)
                .containsExactlyInAnyOrder(Camp.builder().active(true).build());
        assertThat(actualAll).hasSize(2);
    }

    @Test
    void shouldDeleteHallWhenDelete() {
        Camp camp = Camp.builder().build();
        when(hallPort.findById(any())).thenReturn(Optional.of(camp));

        Camp actualDeleted = hallService.delete(UUID.randomUUID(), user);

        assertThat(actualDeleted)
                .isNotNull()
                .isEqualTo(camp);
        verify(hallPort, times(1)).delete(camp);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateWithInvalidData() {
        CreateHallRequest request = new CreateHallRequest(faker.name().firstName());
        when(hallValidator.validate(any(CreateHallRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() -> hallService.create(request, user))
                .isExactlyInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateWithInvalidData() {
        UpdateHallRequest request = new UpdateHallRequest(faker.name().firstName());
        when(hallValidator.validate(any(UpdateHallRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        UUID randomUUID = UUID.randomUUID();

        assertThatThrownBy(() -> hallService.update(randomUUID, request, user))
                .isExactlyInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCreateHallWhenCreateWithValidData() {
        String name = faker.name().firstName();
        CreateHallRequest request = new CreateHallRequest(name);
        when(hallValidator.validate(any(CreateHallRequest.class))).thenReturn(ValidationResult.valid());

        Camp actualCreated = hallService.create(request, user);

        assertThat(actualCreated)
                .isNotNull()
                .extracting(Camp::getName)
                .isEqualTo(name);
        verify(hallPort, times(1)).save(any());
    }

    @Test
    void shouldUpdateHallWhenUpdateWithValidData() {
        String name = faker.name().firstName();
        UpdateHallRequest request = new UpdateHallRequest(name);
        when(hallValidator.validate(any(UpdateHallRequest.class))).thenReturn(ValidationResult.valid());
        when(hallPort.findById(any())).thenReturn(Optional.of(Camp.builder().build()));

        Camp actualUpdated = hallService.update(UUID.randomUUID(), request, user);

        assertThat(actualUpdated)
                .isNotNull()
                .extracting(Camp::getName)
                .isEqualTo(name);
        verify(hallPort, times(1)).save(any());
    }

    @Test
    void shouldActivateWhenActivate() {
        when(hallPort.findById(any()))
                .thenReturn(Optional.of(Camp.builder().active(true).build()))
                .thenReturn(Optional.of(Camp.builder().active(false).build()));

        Camp activeCamp = hallService.activate(UUID.randomUUID(), user);
        Camp activetedCamp = hallService.activate(UUID.randomUUID(), user);

        assertThat(activeCamp).isNotNull().extracting(Camp::getActive).isEqualTo(true);
        assertThat(activetedCamp).isNotNull().extracting(Camp::getActive).isEqualTo(true);

        verify(hallPort, times(1)).save(activetedCamp);
    }

    @Test
    void shouldDeactivateWhenActivate() {
        when(hallPort.findById(any()))
                .thenReturn(Optional.of(Camp.builder().active(true).build()))
                .thenReturn(Optional.of(Camp.builder().active(false).build()));

        Camp deactivatedCamp = hallService.deactivate(UUID.randomUUID(), user);
        Camp inactiveCamp = hallService.deactivate(UUID.randomUUID(), user);

        assertThat(deactivatedCamp).isNotNull().extracting(Camp::getActive).isEqualTo(false);
        assertThat(inactiveCamp).isNotNull().extracting(Camp::getActive).isEqualTo(false);

        verify(hallPort, times(1)).save(deactivatedCamp);
    }

    @Test
    void shouldReturnTablesWhenGetTablesByHallId() {
        when(hallPort.findById(any())).thenReturn(Optional.of(Camp.builder().id(UUID.randomUUID()).build()));
        when(expeditionPort.findAllWhereHallId(any())).thenReturn(List.of(
                Expedition.builder().active(true).build(),
                Expedition.builder().active(true).build()
        ));

        List<Expedition> tables = hallService.getTablesByHallId(UUID.randomUUID());

        assertThat(tables)
                .hasSize(2);
    }
}