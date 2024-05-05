package com.konstantion.controllers.table;

import com.konstantion.call.CallService;
import com.konstantion.dto.call.converter.CallMapper;
import com.konstantion.dto.call.dto.CallDto;
import com.konstantion.dto.call.dto.CreateCallRequestDto;
import com.konstantion.expedition.Expedition;
import com.konstantion.response.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.konstantion.call.Purpose.CALL_BILL;
import static com.konstantion.call.Purpose.CALL_GUIDE;
import static java.util.Map.of;

@RestController
@RequestMapping("/table-api/calls")
public record TableCallController(
        CallService callService
) {
    private static final CallMapper callMapper = CallMapper.INSTANCE;

    @GetMapping("/waiter")
    public ResponseDto callWaiter(
            @AuthenticationPrincipal Expedition table
    ) {
        CreateCallRequestDto createCallRequest = new CreateCallRequestDto(
                CALL_GUIDE,
                table.getId()
        );

        CallDto dto = callMapper.toDto(callService.createCall(
                callMapper.toCreateCallRequest(createCallRequest)
        ));

        return ResponseDto.builder()
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .message("Created call")
                .timeStamp(LocalDateTime.now())
                .data(of("call", dto))
                .build();
    }

    @GetMapping("/bill")
    public ResponseDto callBill(
            @AuthenticationPrincipal Expedition table
    ) {
        CreateCallRequestDto createCallRequest = new CreateCallRequestDto(
                CALL_BILL,
                table.getId()
        );

        CallDto dto = callMapper.toDto(callService.createCall(
                callMapper.toCreateCallRequest(createCallRequest)
        ));

        return ResponseDto.builder()
                .status(HttpStatus.OK)
                .statusCode(HttpStatus.OK.value())
                .message("Created call")
                .timeStamp(LocalDateTime.now())
                .data(of("call", dto))
                .build();
    }
}
