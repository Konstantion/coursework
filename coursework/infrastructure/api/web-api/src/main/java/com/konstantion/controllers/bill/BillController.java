package com.konstantion.controllers.bill;

import com.konstantion.dto.bill.converter.BillMapper;
import com.konstantion.dto.bill.dto.BillDto;
import com.konstantion.dto.bill.dto.CreateBillRequestDto;
import com.konstantion.log.LogService;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@RestController
@RequestMapping("/web-api/bills")
public record BillController(
        LogService billService
) {
    private static final BillMapper billMapper = BillMapper.INSTANCE;

    @GetMapping
    public ResponseDto getAllActiveBills() {
        List<BillDto> dtos = billMapper.toDto(billService.getAll());

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All active bills successfully returned")
                .timeStamp(now())
                .data(Map.of(BILLS, dtos))
                .build();
    }


    @GetMapping("/{id}")
    public ResponseDto getBillById(
            @PathVariable("id") UUID id
    ) {
        BillDto dto = billMapper.toDto(billService.getById(id));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Bill with id %s successfully returned", id))
                .timeStamp(now())
                .data(Map.of(BILL, dto))
                .build();
    }

    @PostMapping
    public ResponseDto createBill(
            @RequestBody CreateBillRequestDto createBillRequestDto,
            @AuthenticationPrincipal User user
    ) {
        BillDto dto = billMapper.toDto(
                billService.create(
                        billMapper.toCreateBillRequest(createBillRequestDto),
                        user
                )
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("Bill successfully created")
                .timeStamp(now())
                .data(Map.of(BILL, dto))
                .build();
    }

    @PutMapping("/{id}/close")
    public ResponseDto closeBill(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        BillDto dto = billMapper.toDto(billService.close(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Bill with id %s successfully closed", id))
                .timeStamp(now())
                .data(Map.of(BILL, dto))
                .build();
    }

    @GetMapping(value = "/{id}/generate-pdf", produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printBill(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        byte[] pdfBytes = billService.getPdfBytesById(id, user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(format("bill-%s.pdf", id)).build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
