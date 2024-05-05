package com.konstantion.controllers.hall;

import com.konstantion.camp.CampService;
import com.konstantion.dto.hall.converter.HallMapper;
import com.konstantion.dto.hall.dto.CreateHallRequestDto;
import com.konstantion.dto.hall.dto.HallDto;
import com.konstantion.dto.hall.dto.UpdateHallRequestDto;
import com.konstantion.dto.table.converter.TableMapper;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import static com.konstantion.utils.EntityNameConstants.HALL;
import static com.konstantion.utils.EntityNameConstants.HALLS;
import static com.konstantion.utils.EntityNameConstants.TABLES;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/halls")
public record AdminHallController(
        CampService campService
) {
    private static final HallMapper hallMapper = HallMapper.INSTANCE;
    private static final TableMapper tableMapper = TableMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getAllHalls() {
        List<HallDto> dtos = hallMapper.toDto(campService.getAll(false));

        return ResponseDto.builder()
                .message("All active halls successfully returned")
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALLS, dtos))
                .build();
    }

    @PostMapping
    public ResponseDto createHall(
            @RequestBody CreateHallRequestDto createHallRequestDto,
            @AuthenticationPrincipal User user
    ) {
        HallDto dto = hallMapper.toDto(campService.create(
                hallMapper.toCreateHallRequest(createHallRequestDto),
                user
        ));

        return ResponseDto.builder()
                .message("Hall successfully created")
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseDto updateHall(
            @PathVariable("id") UUID id,
            @RequestBody UpdateHallRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        HallDto dto = hallMapper.toDto(
                campService.update(
                        id,
                        hallMapper.toUpdateHallRequest(requestDto),
                        user
                )
        );

        return ResponseDto.builder()
                .message(format("Hall with id %s successfully updated", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }

    @PutMapping("/{id}/activate")
    public ResponseDto activateHall(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        HallDto dto = hallMapper.toDto(campService.activate(id, user));

        return ResponseDto.builder()
                .message(format("Hall with id %s successfully activated", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }

    @GetMapping("/{id}/tables")
    public ResponseDto getTablesByHallId(
            @PathVariable("id") UUID id
    ) {
        List<TableDto> dtos = tableMapper.toDto(campService.getTablesByHallId(id, false));
        return ResponseDto.builder()
                .message(format("Tables for hall with id %s", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(TABLES, dtos))
                .build();
    }

    @PutMapping("/{id}/deactivate")
    public ResponseDto deactivateHall(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        HallDto dto = hallMapper.toDto(campService.deactivate(id, user));

        return ResponseDto.builder()
                .message(format("Hall with id %s successfully deactivated", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto deleteHall(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        HallDto dto = hallMapper.toDto(campService.delete(id, user));

        return ResponseDto.builder()
                .message(format("Hall with id %s successfully deleted", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }
}
