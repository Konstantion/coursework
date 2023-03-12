package com.konstantion.controller;

import com.konstantion.product.Product;
import com.konstantion.product.ProductService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import com.konstantion.product.dto.CreationProductDto;
import com.konstantion.product.dto.ProductDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public record CliProductController(ProductService productService) {

    public ProductDto addProduct(CreationProductDto creationProductDto) {
        return productService.create(creationProductDto, null);
    }

    public List<ProductDto> getProducts(Sort.Direction direction, String parameter) {
        return productService.getAll(direction, parameter);
    }

    public List<Map.Entry<ProductDto, Double>> getProductsWithRating() {
        return null;
    }

    public ProductDto deleteProduct(UUID uuid) {
        return productService.delete(uuid);
    }
}
