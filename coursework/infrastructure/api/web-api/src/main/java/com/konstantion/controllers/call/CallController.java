package com.konstantion.controllers.call;

import com.konstantion.call.CallService;
import com.konstantion.dto.call.converter.CallMapper;
import com.konstantion.dto.call.dto.CallDto;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.CALL;
import static com.konstantion.utils.EntityNameConstants.CALLS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/web-api/calls")
public record CallController(
        CallService callService
) {
    private static final CallMapper callMapper = CallMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getCallsByUser(
            @AuthenticationPrincipal User user
    ) {
        List<CallDto> dtos = callMapper.toDto(callService.getAllByUser(user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("All calls for user with id %s", user.getId()))
                .data(Map.of(CALLS, dtos))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto closeCall(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        CallDto dto = callMapper.toDto(callService.closeCall(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Call with id %s successfully closed", dto.id()))
                .data(Map.of(CALL, dto))
                .build();
    }
}
