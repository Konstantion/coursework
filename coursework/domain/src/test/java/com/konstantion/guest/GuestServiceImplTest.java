package com.konstantion.guest;

import com.github.javafaker.Faker;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.guest.model.CreateGuestRequest;
import com.konstantion.guest.model.UpdateGuestRequest;
import com.konstantion.guest.validator.GuestValidator;
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
class GuestServiceImplTest {
    @Mock
    GuestPort guestPort;
    @Mock
    GuestValidator guestValidator;
    @Mock
    User user;
    @InjectMocks
    GuestServiceImpl guestService;
    Faker faker;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        faker = new Faker();
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCallMethodsWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> guestService.activate(null, user)).isExactlyInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> guestService.deactivate(null, user)).isExactlyInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> guestService.delete(null, user)).isExactlyInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> guestService.create(null, user)).isExactlyInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> guestService.update(null, null, user)).isExactlyInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        when(guestPort.findById(any())).thenReturn(Optional.empty());
        UUID randomUUID = UUID.randomUUID();

        assertThatThrownBy(() -> guestService.getById(randomUUID))
                .isExactlyInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnGuestWhenGetByIdWithExistingId() {
        when(guestPort.findById(any())).thenReturn(Optional.of(Guest.builder().build()));

        Guest actual = guestService.getById(UUID.randomUUID());

        assertThat(actual).isNotNull();
    }

    @Test
    void shouldReturnGuestsWhenGetAll() {
        when(guestPort.findAll()).thenReturn(List.of(
                Guest.builder().active(true).build(),
                Guest.builder().active(false).build()
        ));

        List<Guest> actualActive = guestService.getAll(true);
        List<Guest> actualAll = guestService.getAll(false);

        assertThat(actualActive)
                .hasSize(1)
                .containsExactlyInAnyOrder(Guest.builder().active(true).build());
        assertThat(actualAll).hasSize(2);
    }

    @Test
    void shouldDeleteGuestWhenDelete() {
        Guest hall = Guest.builder().build();
        when(guestPort.findById(any())).thenReturn(Optional.of(hall));

        Guest actualDeleted = guestService.delete(UUID.randomUUID(), user);

        assertThat(actualDeleted)
                .isNotNull()
                .isEqualTo(hall);
        verify(guestPort, times(1)).delete(hall);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateWithInvalidData() {
        CreateGuestRequest request = new CreateGuestRequest(faker.name().firstName(), faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(CreateGuestRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() -> guestService.create(request, user))
                .isExactlyInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateWithInvalidData() {
        UpdateGuestRequest request = new UpdateGuestRequest(faker.name().firstName(), faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(UpdateGuestRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        UUID randomUUID = UUID.randomUUID();

        assertThatThrownBy(() -> guestService.update(randomUUID, request, user))
                .isExactlyInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCreateGuestWhenCreateWithValidData() {
        String name = faker.name().firstName();
        CreateGuestRequest request = new CreateGuestRequest(name, faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(CreateGuestRequest.class))).thenReturn(ValidationResult.valid());
        when(guestPort.findByName(any())).thenReturn(Optional.empty());

        Guest actualCreated = guestService.create(request, user);

        assertThat(actualCreated)
                .isNotNull()
                .extracting(Guest::getName)
                .isEqualTo(name);
        verify(guestPort, times(1)).save(any());
    }

    @Test
    void shouldUpdateGuestWhenUpdateWithValidData() {
        String name = faker.name().firstName();
        UpdateGuestRequest request = new UpdateGuestRequest(name, faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(UpdateGuestRequest.class))).thenReturn(ValidationResult.valid());
        when(guestPort.findById(any())).thenReturn(Optional.of(Guest.builder().name("name").active(true).build()));
        when(guestPort.findByName(any())).thenReturn(Optional.empty());

        Guest actualUpdated = guestService.update(UUID.randomUUID(), request, user);

        assertThat(actualUpdated)
                .isNotNull()
                .extracting(Guest::getName)
                .isEqualTo(name);
        verify(guestPort, times(1)).save(any());
    }

    @Test
    void shouldThrowBadRequestExceptionWhenCreateExistingName() {
        String name = faker.name().firstName();
        CreateGuestRequest request = new CreateGuestRequest(name, faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(CreateGuestRequest.class))).thenReturn(ValidationResult.valid());
        when(guestPort.findByName(any())).thenReturn(Optional.of(Guest.builder().build()));

        assertThatThrownBy(() -> guestService.create(request, user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenUpdateWithExistingName() {
        String name = faker.name().firstName();
        UpdateGuestRequest request = new UpdateGuestRequest(name, faker.phoneNumber().cellPhone(), faker.number().randomDouble(2, 0, 100));
        when(guestValidator.validate(any(UpdateGuestRequest.class))).thenReturn(ValidationResult.valid());
        when(guestPort.findById(any())).thenReturn(Optional.of(Guest.builder().name("name").active(true).build()));
        when(guestPort.findByName(any())).thenReturn(Optional.of(Guest.builder().build()));
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> guestService.update(id, request, user))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldActivateWhenActivate() {
        when(guestPort.findById(any()))
                .thenReturn(Optional.of(Guest.builder().active(true).build()))
                .thenReturn(Optional.of(Guest.builder().active(false).build()));

        Guest activeGuest = guestService.activate(UUID.randomUUID(), user);
        Guest activetedGuest = guestService.activate(UUID.randomUUID(), user);

        assertThat(activeGuest).isNotNull().extracting(Guest::getActive).isEqualTo(true);
        assertThat(activetedGuest).isNotNull().extracting(Guest::getActive).isEqualTo(true);

        verify(guestPort, times(1)).save(activetedGuest);
    }

    @Test
    void shouldDeactivateWhenActivate() {
        when(guestPort.findById(any()))
                .thenReturn(Optional.of(Guest.builder().active(true).build()))
                .thenReturn(Optional.of(Guest.builder().active(false).build()));

        Guest deactivatedGuest = guestService.deactivate(UUID.randomUUID(), user);
        Guest inactiveGuest = guestService.deactivate(UUID.randomUUID(), user);

        assertThat(deactivatedGuest).isNotNull().extracting(Guest::getActive).isEqualTo(false);
        assertThat(inactiveGuest).isNotNull().extracting(Guest::getActive).isEqualTo(false);

        verify(guestPort, times(1)).save(deactivatedGuest);
    }
}