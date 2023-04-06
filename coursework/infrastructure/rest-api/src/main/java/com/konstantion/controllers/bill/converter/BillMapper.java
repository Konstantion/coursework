package com.konstantion.controllers.bill.converter;

import com.konstantion.bill.Bill;
import com.konstantion.controllers.bill.dto.BillDto;
import com.konstantion.controllers.bill.dto.CreateBillRequestDto;
import com.konstantion.bill.model.CreateBillRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface BillMapper {
    BillMapper INSTANCE = Mappers.getMapper(BillMapper.class);

    BillDto toDto(Bill bill);

    List<BillDto> toDto(List<Bill> bill);

    CreateBillRequest toEntity(CreateBillRequestDto dto);
}
