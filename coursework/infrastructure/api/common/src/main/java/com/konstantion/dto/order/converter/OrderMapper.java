package com.konstantion.dto.order.converter;

import com.konstantion.dto.order.dto.OrderDto;
import com.konstantion.dto.order.dto.OrderProductsRequestDto;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.model.OrderProductsRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "tableId", source = "expeditionId")
    @Mapping(target = "productsId", source = "gearsId")
    @Mapping(target = "billId", source = "logId")
    OrderDto toDto(Equipment equipment);

    @Mapping(target = "tableId", source = "expeditionId")
    @Mapping(target = "productsId", source = "gearId")
    @Mapping(target = "billId", source = "logId")
    List<OrderDto> toDto(List<Equipment> equipment);

    OrderProductsRequest toOrderProductsRequest(OrderProductsRequestDto dto);
}
