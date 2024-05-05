package com.konstantion.dto.hall.converter;

import com.konstantion.camp.Camp;
import com.konstantion.camp.model.CreateHallRequest;
import com.konstantion.camp.model.UpdateHallRequest;
import com.konstantion.dto.hall.dto.CreateHallRequestDto;
import com.konstantion.dto.hall.dto.HallDto;
import com.konstantion.dto.hall.dto.UpdateHallRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface HallMapper {
    HallMapper INSTANCE = Mappers.getMapper(HallMapper.class);

    HallDto toDto(Camp camp);

    List<HallDto> toDto(List<Camp> entities);

    CreateHallRequest toCreateHallRequest(CreateHallRequestDto createHallRequestDto);

    UpdateHallRequest toUpdateHallRequest(UpdateHallRequestDto updateHallRequestDto);
}
