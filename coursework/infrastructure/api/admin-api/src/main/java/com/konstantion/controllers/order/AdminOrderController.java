package com.konstantion.controllers.order;

import com.konstantion.dto.order.converter.OrderMapper;
import com.konstantion.dto.order.dto.OrderDto;
import com.konstantion.equipment.EquipmentService;
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

import static com.konstantion.utils.EntityNameConstants.ORDER;
import static com.konstantion.utils.EntityNameConstants.ORDERS;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/orders")
public record AdminOrderController(
        EquipmentService equipmentService
) {
    private static final OrderMapper orderMapper = OrderMapper.INSTANCE;

    @GetMapping()
    public ResponseDto getAllOrders() {
        List<OrderDto> dtos = orderMapper.toDto(equipmentService.getAll(false));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message("All orders successfully returned")
                .data(Map.of(ORDERS, dtos))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto deleteOrderById(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal User user
    ) {
        OrderDto dto = orderMapper.toDto(equipmentService.delete(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Order with id %s successfully deleted", id))
                .data(Map.of(ORDER, dto))
                .build();
    }
}
