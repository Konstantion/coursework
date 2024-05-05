package com.konstantion.controllers.bill;

import com.konstantion.dto.bill.converter.BillMapper;
import com.konstantion.dto.bill.dto.BillDto;
import com.konstantion.log.LogService;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.BILL;
import static com.konstantion.utils.EntityNameConstants.BILLS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/bills")
public record AdminBillController(
        LogService billService
) {
    private static final BillMapper billMapper = BillMapper.INSTANCE;

    @GetMapping
    public ResponseDto getAllBills() {
        List<BillDto> dtos = billMapper.toDto(billService.getAll(false));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All bills successfully returned")
                .timeStamp(now())
                .data(Map.of(BILLS, dtos))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto cancelBill(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        BillDto dto = billMapper.toDto(billService.cancel(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Bill with id %s successfully canceled", id))
                .timeStamp(now())
                .data(Map.of(BILL, dto))
                .build();
    }

    @PutMapping("/{id}/activate")
    public ResponseDto activateBill(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        BillDto dto = billMapper.toDto(billService.activate(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Bill with id %s successfully activated", id))
                .timeStamp(now())
                .data(Map.of(BILL, dto))
                .build();
    }
}
