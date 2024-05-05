package com.konstantion.dto.product.converter;

import com.konstantion.dto.product.dto.CreateProductRequestDto;
import com.konstantion.dto.product.dto.ProductDto;
import com.konstantion.dto.product.dto.UpdateProductRequestDto;
import com.konstantion.gear.Gear;
import com.konstantion.gear.model.CreateProductRequest;
import com.konstantion.gear.model.GetProductsRequest;
import com.konstantion.gear.model.UpdateProductRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto toDto(Gear entity);

    List<ProductDto> toDto(List<Gear> entities);

    CreateProductRequest toCreateProductRequest(CreateProductRequestDto productRequestDto);

    default GetProductsRequest toGetProductsRequest(
            int pageNumber,
            int pageSize,
            String fieldName,
            String searchPattern,
            UUID categoryUuid,
            boolean ascending
    ) {
        return new GetProductsRequest(
                pageNumber,
                pageSize,
                fieldName,
                searchPattern,
                categoryUuid,
                ascending
        );
    }

    default Page<ProductDto> toDto(Page<Gear> entities) {
        if (isNull(entities)) {
            return null;
        }
        List<ProductDto> dtos = entities.stream().map(this::toDto).toList();
        return new PageImpl<>(dtos, entities.getPageable(), entities.getTotalElements());
    }

    UpdateProductRequest toUpdateProductRequest(UpdateProductRequestDto dto);
}
