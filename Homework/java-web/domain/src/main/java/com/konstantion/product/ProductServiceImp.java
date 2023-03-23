package com.konstantion.product;

import com.google.common.collect.Lists;
import com.konstantion.category.CategoryService;
import com.konstantion.exceptions.BadRequestException;
import com.konstantion.file.MultipartFileValidator;
import com.konstantion.product.dto.CreationProductDto;
import com.konstantion.product.dto.ProductDto;
import com.konstantion.product.dto.UpdateProductDto;
import com.konstantion.product.validator.ProductValidator;
import com.konstantion.review.ReviewMapper;
import com.konstantion.upload.UploadService;
import com.konstantion.utils.validator.ValidationResult;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.springframework.data.domain.Sort.Direction.ASC;

public record ProductServiceImp(ProductValidator productValidator,
                                MultipartFileValidator fileValidator,
                                ProductRepository productRepository,

                                UploadService uploadService,
                                CategoryService categoryService)
        implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImp.class);
    private static final ProductMapper productMapper = ProductMapper.INSTANCE;
    private static final ReviewMapper reviewMapper = ReviewMapper.INSTANCE;
    private static final String FAILED_TO_CREATE_MESSAGE = "Failed to create product, given data is invalid";
    private static final String FAILED_TO_UPDATE_MESSAGE = "Failed to update product, given data is invalid";


    @Override
    public ProductDto create(CreationProductDto createProductDto, MultipartFile file) {
        ValidationResult validationResult = productValidator
                .validate(createProductDto)
                .combine(fileValidator.validate(file));
        ValidationResult.validOrThrow(validationResult, FAILED_TO_CREATE_MESSAGE);
        //Check if category exist
        if (nonNull(createProductDto.categoryUuid())) {
            categoryService.getCategoryById(createProductDto.categoryUuid());
        }

        Product product = productMapper.toEntity(createProductDto);
        byte[] imageBytes = uploadService.getFileBytes(file);

        product = product
                .setCreatedAt(LocalDateTime.now())
                .setImageBytes(imageBytes);

        product = productRepository.save(product);

        ProductDto productDto = productMapper.toDto(product);

        logger.info("Product {} successfully created", productDto);

        return productDto;
    }

    @Override
    public ProductDto getById(UUID uuid) {
        Product product = getProductByIdOrThrow(uuid);


        logger.info("Product with id {} successfully returned", uuid);
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto delete(UUID uuid) {
        Product product = getProductByIdOrThrow(uuid);

        productRepository.deleteById(uuid);

        logger.info("Product with id {} successfully delete", uuid);
        return productMapper.toDto(product);
    }

    @Override
    public ProductDto update(UUID uuid, UpdateProductDto updateDto) {
        Product product = getProductByIdOrThrow(uuid);

        ValidationResult validationResult = productValidator
                .validate(updateDto);
        //Check if file is present for update
        if (nonNull(updateDto.file()) && !updateDto.file().isEmpty()) {
            validationResult = validationResult.combine(fileValidator.validate(updateDto.file()));
        }

        ValidationResult.validOrThrow(validationResult, FAILED_TO_UPDATE_MESSAGE);

        //Check if category exist
        if(nonNull(updateDto.categoryUuid())) {
            categoryService.getCategoryById(updateDto.categoryUuid());
        }

        product = updateProduct(product, updateDto);

        product = productRepository.save(product);

        logger.info("Product with uuid {} successfully updated", uuid);
        return productMapper.toDto(product);
    }

    @Override
    public List<ProductDto> getAll() {
        return getAll(ASC, "name");
    }

    @Override
    public List<ProductDto> getAll(Sort.Direction sortOrder, String fieldName) {
        return getAll(sortOrder, fieldName, "");
    }

    @Override
    public List<ProductDto> getAll(Sort.Direction sortOrder, String fieldName, String namePattern) {
        return productRepository
                .findAll().stream()
                .map(productMapper::toDto)
                .sorted(getComparator(Sort.by(sortOrder, fieldName)))
                .filter(dto -> containsIgnoreCase(dto.name(), namePattern)).toList();
    }

    @Override
    public Page<ProductDto> getAll(
            Integer pageNumber, Integer pageSize,
            String sortBy, String searchPattern,
            UUID categoryUuid, boolean ascending) {
        sortBy = sortBy.toLowerCase();
        if (nonNull(categoryUuid)) {
            categoryService.getCategoryById(categoryUuid);
        }
        if (!isFieldValidForSearch(sortBy)) {
            throw new BadRequestException(format("Sort products by %s doesn't provided", sortBy));
        }
        sortBy = ascending ? sortBy + " ASC " : sortBy + " DESC ";

        pageNumber = Math.max(pageNumber, 1);
        pageSize = Math.max(pageSize, 1);

        Page<Product> products = productRepository
                .findAll(pageNumber, pageSize, sortBy, searchPattern, categoryUuid);

        return productMapper.toDto(products);
    }

    @Override
    public String getProductImageEncoded(UUID uuid) {
        Product product = getProductByIdOrThrow(uuid);

        return Base64.encodeBase64String(product.imageBytes());
    }

    @Override
    public byte[] getProductImageBytes(UUID uuid) {
        Product product = getProductByIdOrThrow(uuid);
        return product.imageBytes();
    }

    public Comparator<ProductDto> getComparator(Sort sort) {
        Comparator<ProductDto> comparator;
        Sort.Order order = sort.iterator().next();
        comparator = switch (order.getProperty()) {
            case "price" -> Comparator.comparing(ProductDto::price);
            case "name" -> Comparator.comparing(ProductDto::name);
            default -> Comparator.comparing(ProductDto::price);
        };

        if (order.getDirection().equals(Sort.Direction.DESC)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    public boolean isFieldValidForSearch(String field) {
        List<String> validFields = Lists.newArrayList("name", "price", "rating");
        return validFields.contains(field);
    }

    private Product getProductByIdOrThrow(UUID uuid) {
        return productRepository.findById(uuid).orElseThrow(() ->
                new BadRequestException(format("Product with uuid %s doesn't exist", uuid)
                ));
    }

    private Product updateProduct(Product target, UpdateProductDto updateDto) {
        byte[] imageBytes = target.imageBytes();
        if (nonNull(updateDto.file()) && !updateDto.file().isEmpty()) {
            imageBytes = uploadService.getFileBytes(updateDto.file());
        }

        return Product.builder()
                .uuid(target.uuid())
                .name(updateDto.name())
                .price(updateDto.price())
                .description(updateDto.description())
                .imageBytes(imageBytes)
                .userUuid(target.userUuid())
                .createdAt(target.createdAt())
                .categoryUuid(updateDto.categoryUuid())
                .build();
    }
}
