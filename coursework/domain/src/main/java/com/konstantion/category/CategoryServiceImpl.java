package com.konstantion.category;

import com.konstantion.category.model.CreateCategoryRequest;
import com.konstantion.category.model.UpdateCategoryRequest;
import com.konstantion.category.validator.CategoryValidator;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CREATE_CATEGORY;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.user.Permission.UPDATE_CATEGORY;
import static java.util.Objects.requireNonNullElse;

@Component
public record CategoryServiceImpl(
        CategoryPort categoryPort,
        CategoryValidator categoryValidator
) implements CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public List<Category> getAll() {
        List<Category> categories = categoryPort.findAll();
        logger.info("All categories successfully returned");
        return categories;
    }

    @Override
    public Category create(CreateCategoryRequest request, User user) {
        if (user.hasNoPermission(SUPER_USER)
                && user.hasNoPermission(CREATE_CATEGORY)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        categoryValidator.validate(request).validOrTrow();

        Category category = Category.builder()
                .name(request.name())
                .creatorId(user.getId())
                .build();

        categoryPort.save(category);
        logger.info("Category successfully created and returned");
        return category;
    }

    @Override
    public Category deleteById(UUID id, User user) {
        if (user.hasNoPermission(SUPER_USER)
                && user.hasNoPermission(CREATE_CATEGORY)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        Category category = getByIdOrThrow(id);

        categoryPort.delete(category);
        logger.info("Category with id {} successfully created and returned", id);
        return category;
    }

    @Override
    public Category update(UUID id, UpdateCategoryRequest request, User user) {
        if (user.hasNoPermission(SUPER_USER)
                && user.hasNoPermission(UPDATE_CATEGORY)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }
        categoryValidator.validate(request).validOrTrow();

        Category category = getByIdOrThrow(id);

        updateCategory(category, request);

        categoryPort.save(category);

        logger.info("Category with id {} successfully updated and returned", id);
        return category;
    }

    private void updateCategory(Category category, UpdateCategoryRequest request) {
        category.setName(requireNonNullElse(request.name(), category.getName()));
    }

    @Override
    public Category getById(UUID id) {
        Category category = getByIdOrThrow(id);
        logger.info("Category with id {} successfully returned", id);
        return category;
    }

    private Category getByIdOrThrow(UUID id) {
        return categoryPort.findById(id)
                .orElseThrow(nonExistingIdSupplier(Category.class, id));
    }
}
