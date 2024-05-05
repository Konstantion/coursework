package com.konstantion.dto.bill.converter;

import com.konstantion.dto.bill.dto.BillDto;
import com.konstantion.dto.bill.dto.CreateBillRequestDto;
import com.konstantion.log.Log;
import com.konstantion.log.model.CreateBillRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BillMapper {
    BillMapper INSTANCE = Mappers.getMapper(BillMapper.class);


    @Mapping(target = "waiterId", source = "guideId")
    @Mapping(target = "orderId", source = "equipmentId")
    BillDto toDto(Log log);

    @Mapping(target = "waiterId", source = "guideId")
    @Mapping(target = "orderId", source = "equipmentId")
    List<BillDto> toDto(List<Log> log);

    CreateBillRequest toCreateBillRequest(CreateBillRequestDto dto);
}

