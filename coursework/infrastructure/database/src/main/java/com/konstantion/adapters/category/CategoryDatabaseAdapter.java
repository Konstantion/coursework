package com.konstantion.adapters.category;

import com.konstantion.category.Category;
import com.konstantion.category.CategoryPort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Repository
public class CategoryDatabaseAdapter implements CategoryPort {
    public static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.category
            WHERE id = :id;
            """;
    public static final String DELETE_QUERY = """
            DELETE FROM public.category
            WHERE id = :id;
            """;
    public static final String SAVE_QUERY = """
            INSERT INTO public.category (name, creator_id)
            VALUES (:name, :creatorId);
            """;
    public static final String UPDATE_QUERY = """
            UPDATE public.category
            SET name = :name,
                creator_id = :creatorId
            WHERE id = :id;
            """;
    public static final String FIND_ALL_QUERY = """
            SELECT * FROM public.category;
            """;
    public static final String DELETE_ALL_QUERY = """
            DELETE FROM public.category
            WHERE true;
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Category> categoryRowMapper;

    public CategoryDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Category> categoryRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.categoryRowMapper = categoryRowMapper;
    }

    @Override
    public List<Category> findAll() {
        return jdbcTemplate.query(
                FIND_ALL_QUERY,
                categoryRowMapper
        );
    }

    @Override
    public Optional<Category> findById(UUID id) {
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                sqlParameterSource,
                categoryRowMapper
        ).stream().findFirst();
    }

    @Override
    public Category save(Category category) {
        if (nonNull(category.getId())) {
            return update(category);
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(category);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        category.setId(generatedId);

        return category;
    }

    @Override
    public void delete(Category category) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", category.getId());
        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private Category update(Category category) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(category);

        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );

        return category;
    }
}
