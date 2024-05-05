package com.konstantion.controllers.hall;

import com.konstantion.camp.CampService;
import com.konstantion.dto.hall.converter.HallMapper;
import com.konstantion.dto.hall.dto.HallDto;
import com.konstantion.dto.table.converter.TableMapper;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.response.ResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/web-api/halls")
public record HallController(
        CampService campService
) {
    private static final HallMapper hallMapper = HallMapper.INSTANCE;
    private static final TableMapper tableMapper = TableMapper.INSTANCE;

    @GetMapping("/{id}")
    public ResponseDto getHallById(
            @PathVariable("id") UUID id
    ) {
        HallDto dto = hallMapper.toDto(campService.getById(id));

        return ResponseDto.builder()
                .message(format("Hall with id %s", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALL, dto))
                .build();
    }

    @GetMapping()
    public ResponseDto getAllActiveHalls() {
        List<HallDto> dtos = hallMapper.toDto(campService.getAll());

        return ResponseDto.builder()
                .message("All active halls")
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(HALLS, dtos))
                .build();
    }

    @GetMapping("/{id}/tables")
    public ResponseDto getTablesByHallId(
            @PathVariable("id") UUID id
    ) {
        List<TableDto> dtos = tableMapper.toDto(campService.getTablesByHallId(id));
        return ResponseDto.builder()
                .message(format("Tables for hall with id %s", id))
                .timeStamp(now())
                .status(OK)
                .statusCode(OK.value())
                .data(Map.of(TABLES, dtos))
                .build();
    }
}
