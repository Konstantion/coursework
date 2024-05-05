package com.konstantion.dto.call.converter;

import com.konstantion.call.Call;
import com.konstantion.call.model.CreateCallRequest;
import com.konstantion.dto.call.dto.CallDto;
import com.konstantion.dto.call.dto.CreateCallRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CallMapper {
    CallMapper INSTANCE = Mappers.getMapper(CallMapper.class);

    @Mapping(target = "tableId", source = "expeditionId")
    @Mapping(target = "waitersId", source = "guidesId")
    CallDto toDto(Call call);

    CreateCallRequest toCreateCallRequest(CreateCallRequestDto dto);

    @Mapping(target = "tableId", source = "expeditionId")
    @Mapping(target = "waitersId", source = "guidesId")
    List<CallDto> toDto(List<Call> entities);
}
