package com.konstantion.category;

import com.konstantion.category.model.CreateCategoryRequest;
import com.konstantion.category.model.UpdateCategoryRequest;
import com.konstantion.category.validator.CategoryValidator;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import com.konstantion.utils.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CategoryServiceImplTest {
    @Mock
    CategoryPort categoryPort;
    @Mock
    CategoryValidator categoryValidator;
    @Mock
    User user;
    @InjectMocks
    CategoryServiceImpl categoryService;
    CreateCategoryRequest createCategoryRequest;
    UpdateCategoryRequest updateCategoryRequest;
    UUID categoryId;
    String categoryName;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
        categoryId = UUID.randomUUID();
        categoryName = "name";

        createCategoryRequest = new CreateCategoryRequest(categoryName);
        updateCategoryRequest = new UpdateCategoryRequest("updated " + categoryName);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(null, user))
                .isExactlyInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> categoryService.update(null, null, user))
                .isExactlyInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> categoryService.deleteById(null, user))
                .isExactlyInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldReturnCategoriesWhenGetAll() {
        when(categoryPort.findAll()).thenReturn(List.of(
                Category.builder().build(),
                Category.builder().build()
        ));

        List<Category> actualCategories = categoryService.getAll();

        assertThat(actualCategories).hasSize(2);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateInvalidCategory() {
        when(categoryValidator.validate(any(CreateCategoryRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() -> categoryService.create(createCategoryRequest, user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCreateCategoryExceptionWhenCreateValidCategory() {
        when(categoryValidator.validate(any(CreateCategoryRequest.class))).thenReturn(ValidationResult.valid());

        Category category = categoryService.create(createCategoryRequest, user);

        assertThat(category).isNotNull();
        verify(categoryPort, times(1)).save(category);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getById(categoryId))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnCategoryWhenGetByIdWithExistingId() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.of(Category.builder().build()));

        Category category = categoryService.getById(categoryId);

        assertThat(category)
                .isNotNull();
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenDeleteByIdWithNonExistingId() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteById(categoryId, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldDeleteWhenDeleteByIdWithExistingId() {
        when(categoryPort.findById(categoryId)).thenReturn(Optional.of(Category.builder().build()));

        Category category = categoryService.deleteById(categoryId, user);

        assertThat(category)
                .isNotNull();
        verify(categoryPort, times(1)).delete(category);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateCategoryInvalidData() {
        when(categoryValidator.validate(any(UpdateCategoryRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() -> categoryService.update(categoryId, updateCategoryRequest, user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNonExistingIdExceptionWhenUpdateCategoryNonExistingId() {
        when(categoryValidator.validate(any(UpdateCategoryRequest.class))).thenReturn(ValidationResult.valid());
        when(categoryPort.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(categoryId, updateCategoryRequest, user))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldUpdateWhenUpdateCategoryValidData() {
        when(categoryValidator.validate(any(UpdateCategoryRequest.class))).thenReturn(ValidationResult.valid());
        when(categoryPort.findById(categoryId)).thenReturn(Optional.of(Category.builder().build()));

        Category actual = categoryService.update(categoryId, updateCategoryRequest, user);
        assertThat(actual.getName())
                .isEqualTo(updateCategoryRequest.name());
        verify(categoryPort, times(1)).save(actual);
    }
}