package com.konstantion.adapters.gear;

import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNullElse;

@Repository
public class GearDatabaseAdapter implements GearPort {
    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.gear
            WHERE id = :id;
            """;
    private static final String SAVE_QUERY = """
            INSERT INTO public.gear (name, price, weight, category_id, image_bytes, description, creator_id, created_at, deactivate_at, active)
            VALUES (:name, :price, :weight, :categoryId, :imageBytes, :description, :creatorId, :createdAt, :deactivateAt, :active)
            """;
    private static final String DELETE_QUERY = """
            DELETE FROM public.gear
            WHERE id = :id;
            """;
    private static final String DELETE_ALL_QUERY = """
            DELETE FROM public.gear
            WHERE true;
            """;
    private static final String UPDATE_QUERY = """
            UPDATE public.gear
            SET name = :name,
                price = :price,
                weight = :weight,
                category_id = :categoryId,
                image_bytes = :imageBytes,
                description = :description,
                created_at = :createdAt,
                deactivate_at = :deactivateAt,
                active = :active
            WHERE id = :id;
            """;
    private static final BiFunction<String, UUID, String> FIND_ALL_QUERY =
            (orderParameter, categoryId) -> {
                String findByCategoryId = categoryId == null ? "" : "AND category_id = :categoryId ";
                return "SELECT * FROM public.gear " +
                        "WHERE LOWER(gear.name) LIKE LOWER(:searchPattern) " +
                        "AND (active = :active OR :inactive) " +
                        findByCategoryId +
                        "ORDER BY " + orderParameter + " LIMIT :limit OFFSET :offset;";
            };
    private static final Function<UUID, String> TOTAL_COUNT_QUERY =
            categoryId -> {
                String findByCategoryId = categoryId == null ? "" : "AND category_id = :categoryId ";
                return "SELECT COUNT(*) FROM public.gear " +
                        "WHERE LOWER(gear.name) LIKE LOWER(:searchPattern) " +
                        "AND (active = :active OR :inactive) " +
                        findByCategoryId + ";";
            };
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Gear> productRowMapper;

    public GearDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Gear> productRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.productRowMapper = productRowMapper;
    }

    @Override
    public Gear save(Gear gear) {
        if (nonNull(gear.getId())) {
            return update(gear);
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(gear);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        gear.setId(generatedId);

        return gear;
    }

    @Override
    public void delete(Gear gear) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", gear.getId());
        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public Optional<Gear> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                productRowMapper
        ).stream().findFirst();
    }

    @Override
    public Page<Gear> findAll(
            int pageNumber,
            int pageSize,
            String orderBy,
            String searchPattern,
            UUID categoryId,
            boolean ascending,
            boolean active
    ) {
        int offset = (pageNumber - 1) * pageSize;
        searchPattern = "%" + searchPattern + "%";
        orderBy = ascending ? orderBy + " ASC " : orderBy + " DESC ";

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("limit", pageSize)
                .addValue("offset", offset)
                .addValue("searchPattern", searchPattern)
                .addValue("categoryId", categoryId)
                .addValue("active", active)
                .addValue("inactive", !active);

        List<Gear> gears = jdbcTemplate.query(
                FIND_ALL_QUERY.apply(orderBy, categoryId),
                parameterSource,
                productRowMapper);

        Integer totalCount = requireNonNullElse(jdbcTemplate.queryForObject(
                TOTAL_COUNT_QUERY.apply(categoryId),
                parameterSource,
                Integer.class
        ), gears.size());

        return new PageImpl<>(gears, PageRequest.of(pageNumber - 1, pageSize), totalCount);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private Gear update(Gear gear) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(gear);
        jdbcTemplate.update(
                UPDATE_QUERY,
                sqlParameterSource
        );
        return gear;
    }
}
