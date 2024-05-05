package com.konstantion.call;

import com.konstantion.call.model.CreateCallRequest;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CLOSE_CALL;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Role.ADMIN;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;

@Component
public record CallServiceImpl(
        CallPort callPort,
        ExpeditionPort expeditionPort,
        SimpMessagingTemplate simpMessagingTemplate
) implements CallService {
    private static final Logger logger = LoggerFactory.getLogger(CallServiceImpl.class);

    @Override
    public List<Call> getAll() {
        List<Call> calls = callPort.findAll();
        logger.info("All calls successfully returned");
        return calls;
    }

    @Override
    public List<Call> getAllByUser(User user) {
        List<Call> calls = callPort.findAll()
                .stream()
                .filter(call -> call.getGuidesId().contains(user.getId()))
                .toList();
        logger.info("All calls for user with id {} successfully returned", user.getId());
        return calls;
    }

    @Override
    public Call createCall(CreateCallRequest request) {
        UUID tableId = request.tableId();
        Purpose purpose = request.purpose();

        Expedition table = expeditionPort.findById(tableId)
                .orElseThrow(nonExistingIdSupplier(Expedition.class, tableId));

        callPort.findByTableIdAndPurpose(tableId, purpose).ifPresent(call -> {
            throw new BadRequestException(format("Call with purpose %s for table %s already exist", purpose, tableId));
        });

        Call call = Call.builder()
                .purpose(purpose)
                .expeditionId(tableId)
                .guidesId(table.getGuidesId())
                .openedAt(now())
                .build();

        callPort.save(call);

        simpMessagingTemplate.convertAndSend("/topic/calls", call);

        logger.info("Call successfully created and returned");
        return call;
    }

    @Override
    public Call closeCall(UUID callId, User user) {
        if (user.hasNoPermission(CLOSE_CALL)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Call call = getByIdOrThrow(callId);

        if (!call.getGuidesId().contains(user.getId())
                && user.hasNoPermission(ADMIN)) {
            throw new BadRequestException(format("Call with id %s doesn't related with user with id %s", callId, user.getId()));
        }

        callPort.delete(call);
        logger.info("Call with id {} successfully closed and returned", callId);
        return call;
    }

    private Call getByIdOrThrow(UUID callId) {
        return callPort.findById(callId)
                .orElseThrow(nonExistingIdSupplier(Call.class, callId));
    }
}
