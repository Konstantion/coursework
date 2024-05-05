package com.konstantion.controllers.table;

import com.konstantion.dto.order.converter.OrderMapper;
import com.konstantion.dto.order.dto.OrderDto;
import com.konstantion.dto.table.converter.TableMapper;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.dto.user.converter.UserMapper;
import com.konstantion.dto.user.dto.UserDto;
import com.konstantion.equipment.EquipmentService;
import com.konstantion.expedition.ExpeditionService;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import com.konstantion.utils.HashMaps;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.ORDER;
import static com.konstantion.utils.EntityNameConstants.TABLE;
import static com.konstantion.utils.EntityNameConstants.TABLES;
import static com.konstantion.utils.EntityNameConstants.USERS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/web-api/tables")
public record TableController(
        ExpeditionService expeditionService,
        EquipmentService equipmentService
) {
    private static final TableMapper tableMapper = TableMapper.INSTANCE;
    private static final OrderMapper orderMapper = OrderMapper.INSTANCE;

    private static final UserMapper userMapper = UserMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getAllActiveTables() {
        List<TableDto> dtos = tableMapper.toDto(expeditionService.getAll());

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("All active tables")
                .timeStamp(now())
                .data(HashMaps.of(TABLES, dtos))
                .build();
    }

    @GetMapping("/{id}")
    public ResponseDto getTableById(
            @PathVariable("id") UUID id
    ) {
        TableDto dto = tableMapper.toDto(expeditionService.getById(id));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Table with id %s", dto.id()))
                .timeStamp(now())
                .data(HashMaps.of(TABLE, dto))
                .build();
    }

    @GetMapping("/{id}/order")
    public ResponseDto getOrderByTableId(
            @PathVariable("id") UUID id
    ) {
        OrderDto dto = orderMapper.toDto(expeditionService.getOrderByTableId(id));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Order for table with id %s", id))
                .timeStamp(now())
                .data(HashMaps.of(ORDER, dto))
                .build();
    }

    @PostMapping("/{id}/order")
    public ResponseDto openTableOrder(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        OrderDto dto = orderMapper.toDto(equipmentService.open(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message("Order successfully created")
                .timeStamp(now())
                .data(HashMaps.of(ORDER, dto))
                .build();
    }

    @GetMapping("/{id}/waiters")
    public ResponseDto getWaitersByTableId(
            @PathVariable("id") UUID id
    ) {
        List<UserDto> dtos = userMapper.toDto(
                expeditionService.getWaitersByTableId(id)
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .message(format("Waiters for table with id %s", id))
                .timeStamp(now())
                .data(HashMaps.of(USERS, dtos))
                .build();
    }
}
