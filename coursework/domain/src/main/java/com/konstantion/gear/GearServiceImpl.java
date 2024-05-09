package com.konstantion.gear;

import com.konstantion.category.Category;
import com.konstantion.category.CategoryPort;
import com.konstantion.exception.BadRequestException;
import com.konstantion.exception.ForbiddenException;
import com.konstantion.file.MultipartFileService;
import com.konstantion.gear.model.CreateProductRequest;
import com.konstantion.gear.model.GetProductsRequest;
import com.konstantion.gear.model.UpdateProductRequest;
import com.konstantion.gear.validator.GearValidators;
import com.konstantion.user.User;
import com.konstantion.utils.validator.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.konstantion.exception.utils.ExceptionMessages.NOT_ENOUGH_AUTHORITIES;
import static com.konstantion.exception.utils.ExceptionUtils.nonExistingIdSupplier;
import static com.konstantion.user.Permission.CHANGE_GEAR_STATE;
import static com.konstantion.user.Permission.CREATE_GEAR;
import static com.konstantion.user.Permission.DELETE_GEAR;
import static com.konstantion.user.Permission.SUPER_USER;
import static com.konstantion.utils.ObjectUtils.requireNonNullOrElseNullable;
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;

@Component
public record GearServiceImpl(
        GearPort gearPort,
        CategoryPort categoryPort,
        GearValidators productValidator,
        MultipartFileService fileService
) implements GearService {
    private static final Logger logger = LoggerFactory.getLogger(GearServiceImpl.class);

    @Override
    public Gear create(CreateProductRequest createProductRequest, User user) {
        if (user.hasNoPermission(CREATE_GEAR)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        ValidationResult validationResult = productValidator
                .validate(createProductRequest);
        validationResult.validOrTrow();

        byte[] imageBytes = fileService.getFileBytes(createProductRequest.image());

        Gear gear = Gear.builder()
                .name(createProductRequest.name())
                .price(createProductRequest.price())
                .description(createProductRequest.description())
                .weight(createProductRequest.weight())
                .active(true)
                .creatorId(user.getId())
                .createdAt(now())
                .categoryId(createProductRequest.categoryId())
                .imageBytes(imageBytes)
                .build();

        gearPort.save(gear);

        logger.info("Gear successfully created and returned");
        return gear;
    }

    @Override
    public Page<Gear> getAll(GetProductsRequest request, boolean onlyActive) {
        UUID categoryId = request.categoryId();
        if (nonNull(categoryId)) {
            categoryPort.findById(categoryId)
                    .orElseThrow(nonExistingIdSupplier(Category.class, categoryId));
        }

        String orderBy = request.orderBy().toLowerCase();
        if (!isOrderByValid(orderBy)) {
            throw new BadRequestException(format("Order by %s isn't provide", orderBy));
        }

        String searchPattern = request.searchPattern().trim();
        int pageNumber = Math.max(request.pageNumber(), 1);
        int pageSize = Math.max(request.pageSize(), 1);
        boolean ascending = request.ascending();

        Page<Gear> products = gearPort.findAll(
                pageNumber,
                pageSize,
                orderBy,
                searchPattern,
                categoryId,
                ascending,
                onlyActive
        );
        logger.info("All gear successfully returned");
        return products;
    }

    @Override
    public Gear delete(UUID productId, User user) {
        if (user.hasNoPermission(DELETE_GEAR)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Gear gear = getByIdOrThrow(productId);

        gearPort.delete(gear);

        logger.info("Gear with id {} successfully deleted and returned", productId);
        return gear;
    }

    @Override
    public Gear update(UUID productId, UpdateProductRequest request, User user) {
        if (user.hasNoPermission(CREATE_GEAR)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        productValidator.validate(request).validOrTrow();

        Gear gear = getByIdOrThrow(productId);

        UUID categoryId = request.categoryId();
        if (nonNull(categoryId)) {
            categoryPort.findById(categoryId)
                    .orElseThrow(nonExistingIdSupplier(Category.class, categoryId));
        }

        MultipartFile file = request.image();
        byte[] imageBytes = null;
        if (nonNull(file)) {
            imageBytes = fileService.getFileBytes(file);
        }

        updateProduct(gear, request, imageBytes);

        gearPort.save(gear);

        logger.info("Gear with id {} successfully updated and returned", productId);
        return gear;
    }

    @Override
    public Gear deactivate(UUID productId, User user) {
        if (user.hasNoPermission(CHANGE_GEAR_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Gear gear = getByIdOrThrow(productId);

        if (!gear.isActive()) {
            logger.info("Gear with id {} is already inactive", productId);
            return gear;
        }

        prepareToDeactivate(gear);
        gearPort.save(gear);

        logger.info("Gear with id {} successfully deactivated and returned", gear);

        return gear;
    }

    @Override
    public Gear activate(UUID productId, User user) {
        if (user.hasNoPermission(CHANGE_GEAR_STATE)
                && user.hasNoPermission(SUPER_USER)) {
            throw new ForbiddenException(NOT_ENOUGH_AUTHORITIES);
        }

        Gear gear = getByIdOrThrow(productId);

        if (gear.isActive()) {
            logger.warn("Gear with id {} is already active", productId);
            return gear;
        }

        prepareToActivate(gear);
        gearPort.save(gear);

        logger.info("Gear with id {} successfully activated and returned", productId);
        return gear;
    }

    @Override
    public Gear getById(UUID productId) {
        Gear gear = getByIdOrThrow(productId);
        logger.info("Gear with id {} successfully returned", productId);
        return gear;
    }

    private void updateProduct(Gear gear, UpdateProductRequest request, byte[] imageBytes) {
        gear.setName(requireNonNullOrElseNullable(request.name(), gear.getName()));
        gear.setPrice(requireNonNullOrElseNullable(request.price(), gear.getPrice()));
        gear.setWeight(requireNonNullOrElseNullable(request.weight(), gear.getWeight()));
        gear.setDescription(requireNonNullOrElseNullable(request.description(), gear.getDescription()));
        gear.setCategoryId(requireNonNullOrElseNullable(request.categoryId(), null));
        gear.setImageBytes(requireNonNullOrElseNullable(imageBytes, gear.getImageBytes()));
    }

    private void prepareToDeactivate(Gear gear) {
        gear.setDeactivateAt(now());
        gear.setActive(false);
    }

    private void prepareToActivate(Gear gear) {
        gear.setDeactivateAt(null);
        gear.setActive(true);
    }

    private Gear getByIdOrThrow(UUID productId) {
        return gearPort.findById(productId)
                .orElseThrow(nonExistingIdSupplier(Gear.class, productId));
    }

    private boolean isOrderByValid(String orderBy) {

        List<String> validOrderBy = List.of("name", "price", "weight");
        return validOrderBy.contains(orderBy);
    }
}
