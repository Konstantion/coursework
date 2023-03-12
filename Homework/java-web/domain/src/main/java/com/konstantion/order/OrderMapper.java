package com.konstantion.order;

import com.konstantion.order.dto.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    default OrderDto toDto(Order entity) {
        Map<UUID, Integer> dtoProducts = entity.products()
                .entrySet().stream().map((entry) -> Map.entry(entry.getKey().uuid(), entry.getValue())).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue
                ));

        return new OrderDto(entity.uuid(), dtoProducts, entity.totalPrice(), entity.userUuid(), entity.status());
    }

    List<OrderDto> toDto(List<Order> entity);
}
