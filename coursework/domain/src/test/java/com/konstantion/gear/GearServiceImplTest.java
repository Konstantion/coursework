package com.konstantion.gear;

import com.konstantion.category.Category;
import com.konstantion.category.CategoryPort;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.exception.NonExistingIdException;
import com.konstantion.exception.ValidationException;
import com.konstantion.file.MultipartFileService;
import com.konstantion.gear.model.CreateProductRequest;
import com.konstantion.gear.model.GetProductsRequest;
import com.konstantion.gear.model.UpdateProductRequest;
import com.konstantion.gear.validator.GearValidators;
import com.konstantion.user.Permission;
import com.konstantion.user.User;
import com.konstantion.utils.validator.ValidationResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GearServiceImplTest {
    @Mock
    GearPort gearPort;
    @Mock
    CategoryPort categoryPort;
    @Mock
    GearValidators productValidator;
    @Mock
    MultipartFileService fileService;
    @Mock
    User user;
    @InjectMocks
    GearServiceImpl productService;

    @BeforeEach
    void setUp() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(false);
    }

    @Test
    void shouldThrowForbiddenExceptionWhenMethodRequirePermissionWithoutPermission() {
        when(user.hasNoPermission(any(Permission.class))).thenReturn(true);

        assertThatThrownBy(() -> productService.activate(null, user))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> productService.deactivate(null, user))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> productService.create(null, user))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> productService.update(null, null, user))
                .isInstanceOf(ForbiddenException.class);

        assertThatThrownBy(() -> productService.delete(null, user))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetByIdWithNonExistingId() {
        UUID nonExistingId = UUID.randomUUID();
        when(gearPort.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(nonExistingId))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldReturnProductWhenGetByIdWithExistingId() {
        when(gearPort.findById(any(UUID.class))).thenReturn(Optional.of(Gear.builder().build()));

        Gear actual = productService.getById(UUID.randomUUID());

        assertThat(actual).isNotNull();
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenGetAllWithNonExistingCategoryId() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getAll(new GetProductsRequest(1, 1, "", "", UUID.randomUUID(), false)))
                .isInstanceOf(NonExistingIdException.class);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenGetAllWithInvalidOrderBy() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.of(Category.builder().build()));

        assertThatThrownBy(() -> productService.getAll(new GetProductsRequest(1, 1, "orderBy", "searchPattern", UUID.randomUUID(), false)))
                .isExactlyInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldReturnAllProductsWhenGetAll() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.of(Category.builder().build()));
        when(gearPort.findAll(anyInt(), anyInt(), anyString(), anyString(), any(UUID.class), anyBoolean(), anyBoolean())).thenReturn(new PageImpl<>(List.of(Gear.builder().build()), Pageable.ofSize(1), 1));
        Page<Gear> products = productService.getAll(new GetProductsRequest(1, 1, "name", "searchPattern", UUID.randomUUID(), false), false);
        assertThat(products)
                .isNotNull()
                .extracting(Page::getContent)
                .matches(list -> list.size() == 1);
    }

    @Test
    void shouldCallMethodFindAllWithValidDataWhenGetAllWithInvalidPageData() {
        when(categoryPort.findById(any(UUID.class))).thenReturn(Optional.of(Category.builder().build()));
        when(gearPort.findAll(anyInt(), anyInt(), anyString(), anyString(), any(UUID.class), anyBoolean(), anyBoolean())).thenReturn(new PageImpl<>(List.of(Gear.builder().build()), Pageable.ofSize(1), 1));
        Page<Gear> products = productService.getAll(new GetProductsRequest(-3124, -3333454, "name", "searchPattern", UUID.randomUUID(), false), false);
        assertThat(products)
                .isNotNull()
                .extracting(Page::getContent)
                .matches(list -> list.size() == 1);
        verify(gearPort, times(1)).findAll(eq(1), eq(1), anyString(), anyString(), any(UUID.class), anyBoolean(), anyBoolean());
    }

    @Test
    void shouldThrowNonExistingIdExceptionWhenDeleteWithNonExistingId() {
        UUID nonExistingId = UUID.randomUUID();
        when(gearPort.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deactivate(nonExistingId, user));
    }

    @Test
    void shouldDeleteProductWhenDeleteWithExistingId() {
        when(gearPort.findById(any())).thenReturn(Optional.of(Gear.builder().build()));

        Gear deleted = productService.delete(UUID.randomUUID(), user);

        assertThat(deleted).isNotNull();

        verify(gearPort, times(1)).delete(deleted);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateOrUpdateWithInvalidData() {
        when(productValidator.validate(any(CreateProductRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));
        when(productValidator.validate(any(UpdateProductRequest.class))).thenReturn(ValidationResult.invalid(Set.of()));

        assertThatThrownBy(() -> productService.create(new CreateProductRequest(null, null, null, null, null, null), user))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> productService.update(UUID.randomUUID(), new UpdateProductRequest(null, null, null, null, null, null), user))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldCreateProductWhenCreateWithValidData() {
        when(productValidator.validate(any(CreateProductRequest.class))).thenReturn(ValidationResult.valid());
        when(fileService.getFileBytes(any())).thenReturn(new byte[0]);

        Gear gear = productService.create(new CreateProductRequest("name", null, null, null, null, null), user);

        assertThat(gear)
                .isNotNull()
                .extracting(Gear::getName).isEqualTo("name");
    }

    @Test
    void shouldUpdateProductWhenUpdateWithValidData() {
        when(productValidator.validate(any(UpdateProductRequest.class))).thenReturn(ValidationResult.valid());
        when(fileService.getFileBytes(any())).thenReturn(new byte[0]);
        when(gearPort.findById(any())).thenReturn(Optional.of(Gear.builder().price(10.4).build()));

        Gear updated = productService.update(UUID.randomUUID(), new UpdateProductRequest("name", null, null, null, null, null), user);

        assertThat(updated)
                .isNotNull()
                .matches(product -> product.getName().equals("name") && product.getPrice().equals(10.4));
    }

    @Test
    void shouldActivateTableWhenActivate() {
        when(gearPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Gear.builder().active(false).build()))
                .thenReturn(Optional.of(Gear.builder().active(true).build()));

        Gear actualActivated = productService.activate(UUID.randomUUID(), user);
        Gear actualActive = productService.activate(UUID.randomUUID(), user);

        Assertions.assertThat(actualActivated)
                .isNotNull()
                .extracting(Gear::isActive).isEqualTo(true);
        Assertions.assertThat(actualActive)
                .isNotNull()
                .extracting(Gear::isActive).isEqualTo(true);

        verify(gearPort, times(1)).save(actualActivated);
    }

    @Test
    void shouldDeactivateTableWhenDeactivate() {
        when(gearPort.findById(any(UUID.class)))
                .thenReturn(Optional.of(Gear.builder().active(true).build()))
                .thenReturn(Optional.of(Gear.builder().active(false).build()));

        Gear actualDeactivated = productService.deactivate(UUID.randomUUID(), user);
        Gear actualInactive = productService.deactivate(UUID.randomUUID(), user);

        Assertions.assertThat(actualDeactivated)
                .isNotNull()
                .extracting(Gear::isActive).isEqualTo(false);
        Assertions.assertThat(actualInactive)
                .isNotNull()
                .extracting(Gear::isActive).isEqualTo(false);

        verify(gearPort, times(1)).save(actualDeactivated);
    }
}