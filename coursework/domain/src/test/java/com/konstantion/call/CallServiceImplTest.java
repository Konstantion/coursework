package com.konstantion.call;

import com.konstantion.call.model.CreateCallRequest;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.user.Permission;
import com.konstantion.user.Role;
import com.konstantion.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(SpringExtension.class)
class CallServiceImplTest {
    @Mock
    CallPort callPort;
    @Mock
    ExpeditionPort expeditionPort;
    @Mock
    SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks
    CallServiceImpl callService;
    @Mock
    User user;
    UUID callId;
    UUID tableId;
    CreateCallRequest request;

    @BeforeEach
    void setUp() {
        callId = UUID.randomUUID();
        tableId = UUID.randomUUID();

        request = new CreateCallRequest(Purpose.CALL_BILL, tableId);
    }

    @Test
    void shouldReturnObjectWhenUserConstructor() {
        assertThat(callService)
                .isNotNull()
                .isInstanceOf(CallService.class);
    }

    @Test
    void shouldReturnAllCallsWhenGetAll() {
        when(callPort.findAll()).thenReturn(
                List.of(
                        Call.builder().build(),
                        Call.builder().build())
        );

        List<Call> actualCalls = callService.getAll();

        assertThat(actualCalls).hasSize(2);
    }

    @Test
    void shouldReturnUserCallsWhenGetAllByUser() {
        UUID userId = UUID.randomUUID();

        when(callPort.findAll()).thenReturn(
                List.of(
                        Call.builder().guidesId(Set.of(userId)).build(),
                        Call.builder().build()
                ));
        when(user.getId()).thenReturn(userId);

        List<Call> actualCalls = callService.getAllByUser(user);

        assertThat(actualCalls).hasSize(1);
    }

    @Test
    void shouldThrowNonExistingIdWhenCreateWithNonExistingTable() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> callService.createCall(request))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowBadRequestWhenCreateCallWithParametersThenAlreadyExist() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().build()));
        when(callPort.findByTableIdAndPurpose(any(UUID.class), any(Purpose.class))).thenReturn(Optional.of(Call.builder().build()));

        assertThatThrownBy(() -> callService.createCall(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCreateWhenCreateCallWithValidParameters() {
        when(expeditionPort.findById(any(UUID.class))).thenReturn(Optional.of(Expedition.builder().build()));
        when(callPort.findByTableIdAndPurpose(any(UUID.class), any(Purpose.class))).thenReturn(Optional.empty());

        Call actualCall = callService.createCall(request);

        assertThat(actualCall.getOpenedAt())
                .isNotNull()
                .isEqualToIgnoringMinutes(now());

        verify(callPort, times(1)).save(any(Call.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenCloseWithoutPermission() {
        UUID randomUUID = UUID.randomUUID();
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> callService.closeCall(randomUUID, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdWhenCloseWithNonExistingCall() {
        UUID randomUUID = UUID.randomUUID();
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        when(callPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> callService.closeCall(randomUUID, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowBadRequestWhenCloseWithUserNotRelatedToCallWithoutPermission() {
        UUID randomUUID = UUID.randomUUID();
        when(user.hasNoPermission(Permission.CLOSE_CALL)).thenReturn(false);
        when(user.hasNoPermission(Permission.SUPER_USER)).thenReturn(true);
        when(user.hasNoPermission(Role.ADMIN)).thenReturn(true);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(callPort.findById(any(UUID.class))).thenReturn(Optional.of(Call.builder().guidesId(Set.of(randomUUID)).build()));

        assertThatThrownBy(() -> callService.closeCall(randomUUID, user))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldCloseCallWhenCloseWithValidParameters() {
        UUID randomUUID = UUID.randomUUID();
        when(user.hasNoPermission(Permission.CLOSE_CALL)).thenReturn(false);
        when(user.hasNoPermission(Permission.SUPER_USER)).thenReturn(true);
        when(user.hasNoPermission(Role.ADMIN)).thenReturn(true);
        when(user.getId()).thenReturn(randomUUID);
        when(callPort.findById(any(UUID.class))).thenReturn(Optional.of(Call.builder().guidesId(Set.of(randomUUID)).build()));

        Call call = callService.closeCall(randomUUID, user);

        verify(callPort, times(1)).delete(call);
    }
}