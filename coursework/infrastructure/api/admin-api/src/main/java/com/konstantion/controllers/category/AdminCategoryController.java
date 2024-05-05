package com.konstantion.controllers.category;

import com.konstantion.category.CategoryService;
import com.konstantion.dto.category.converter.CategoryMapper;
import com.konstantion.dto.category.dto.CategoryDto;
import com.konstantion.dto.category.dto.CreateCategoryRequestDto;
import com.konstantion.dto.category.dto.UpdateCategoryRequestDto;
import com.konstantion.response.ResponseDto;
import com.konstantion.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.CATEGORY;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin-api/categories")
public record AdminCategoryController(
        CategoryService categoryService
) {
    private static final CategoryMapper categoryMapper = CategoryMapper.INSTANCE;

    @PostMapping()
    public ResponseDto createCategory(
            @RequestBody CreateCategoryRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        CategoryDto dto = categoryMapper.toDto(categoryService.create(
                categoryMapper.toCreateCategoryRequest(requestDto),
                user
        ));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message("Category successfully created")
                .data(Map.of(CATEGORY, dto))
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseDto deleteCategoryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        CategoryDto dto = categoryMapper.toDto(categoryService.deleteById(id, user));

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Category with id %s successfully deleted", id))
                .data(Map.of(CATEGORY, dto))
                .build();
    }

    @PutMapping("/{id}")
    public ResponseDto updateCategory(
            @PathVariable UUID id,
            @RequestBody UpdateCategoryRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        CategoryDto dto = categoryMapper.toDto(
                categoryService.update(
                        id,
                        categoryMapper.toUpdateCategoryRequest(requestDto),
                        user
                )
        );

        return ResponseDto.builder()
                .status(OK)
                .statusCode(OK.value())
                .timeStamp(now())
                .message(format("Category with id %s successfully updated", id))
                .data(Map.of(CATEGORY, dto))
                .build();
    }

}
