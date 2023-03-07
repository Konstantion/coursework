package com.konstantion.reporitories.mappers;

import com.konstantion.product.Product;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record ProductRawMapper() implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Product.builder()
                .id(rs.getLong("id"))
                .uuid(rs.getObject("uuid", UUID.class))
                .price(rs.getDouble("price"))
                .name(rs.getString("name"))
                .imagePath(rs.getString("image_path"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .userUuid(rs.getObject("user_uuid", UUID.class))
                .build();
    }
}