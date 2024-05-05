package com.konstantion.dto.table.converter;

import com.konstantion.dto.table.dto.CreateTableRequestDto;
import com.konstantion.dto.table.dto.LoginTableRequestDto;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.dto.table.dto.TableWaitersRequestDto;
import com.konstantion.dto.table.dto.UpdateTableRequestDto;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.model.CreateTableRequest;
import com.konstantion.expedition.model.LoginTableRequest;
import com.konstantion.expedition.model.TableWaitersRequest;
import com.konstantion.expedition.model.UpdateTableRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TableMapper {
    TableMapper INSTANCE = Mappers.getMapper(TableMapper.class);

    @Mapping(target = "tableType", source = "expeditionType")
    @Mapping(target = "hallId", source = "campId")
    @Mapping(target = "waitersId", source = "guidesId")
    @Mapping(target = "orderId", source = "equipmentId")
    TableDto toDto(Expedition table);

    @Mapping(target = "tableType", source = "expeditionType")
    @Mapping(target = "hallId", source = "campId")
    @Mapping(target = "waitersId", source = "guidesId")
    @Mapping(target = "orderId", source = "equipmentId")
    List<TableDto> toDto(List<Expedition> entities);

    LoginTableRequest toLoginTableRequest(LoginTableRequestDto requestDto);

    CreateTableRequest toCreateTableRequest(CreateTableRequestDto requestDto);

    UpdateTableRequest toUpdateTableRequest(UpdateTableRequestDto requestDto);

    TableWaitersRequest toTableWaitersRequest(TableWaitersRequestDto requestDto);
}
