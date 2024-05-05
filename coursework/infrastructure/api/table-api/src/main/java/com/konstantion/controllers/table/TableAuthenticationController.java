package com.konstantion.controllers.table;

import com.konstantion.authentication.AuthenticationService;
import com.konstantion.dto.authentication.converter.AuthenticationMapper;
import com.konstantion.dto.authentication.dto.AuthenticationResponseDto;
import com.konstantion.dto.table.converter.TableMapper;
import com.konstantion.dto.table.dto.LoginTableRequestDto;
import com.konstantion.expedition.ExpeditionService;
import com.konstantion.response.ResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.konstantion.utils.EntityNameConstants.AUTHENTICATION;
import static java.util.Map.of;

@RestController
@RequestMapping("/table-api/authentication")
public record TableAuthenticationController(
        ExpeditionService expeditionService,
        AuthenticationService authenticationService
) {
    public static final String BEARER_PREFIX = "Bearer ";
    private static final TableMapper tableMapper = TableMapper.INSTANCE;
    private static final AuthenticationMapper authenticationMapper = AuthenticationMapper.INSTANCE;

    @PostMapping()
    public ResponseEntity<ResponseDto> authenticate(
            @RequestBody LoginTableRequestDto requestDto
    ) {
        AuthenticationResponseDto responseDto = authenticationMapper.toDto(
                authenticationService.authenticate(
                        tableMapper.toLoginTableRequest(requestDto)
                ));

        String bearerToken = BEARER_PREFIX + responseDto.token();

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .body(ResponseDto.builder()
                        .status(HttpStatus.OK)
                        .statusCode(HttpStatus.OK.value())
                        .message("Successfully authenticated")
                        .timeStamp(LocalDateTime.now())
                        .data(of(AUTHENTICATION, responseDto))
                        .build()
                );
    }
}
