package com.konstantion.adapters.log;

import com.konstantion.log.Log;
import com.konstantion.log.LogPort;
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
public class LogDatabaseAdapter implements LogPort {
    public static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.log
            WHERE id = :id;
            """;
    public static final String FIND_BY_ORDER_ID_QUERY = """
            SELECT * FROM public.log
            WHERE equipment_id = :equipmentId;
            """;
    public static final String SAVE_QUERY = """
            INSERT INTO public.log (guide_id, equipment_id, guest_id, created_at, closed_at, active, price, price_with_discount)
            VALUES (:guideId, :equipmentId, :guestId, :createdAt, :closedAt, :active, :price, :priceWithDiscount);
            """;
    public static final String UPDATE_QUERY = """
            UPDATE public.log
            SET guide_id = :guideId,
                equipment_id = :equipmentId,
                guest_id = :guestId,
                created_at = :createdAt,
                closed_at = :closedAt,
                active = :active,
                price = :price,
                price_with_discount = :priceWithDiscount
            WHERE id = :id;
            """;
    public static final String DELETE_QUERY = """
            DELETE FROM public.log
            WHERE id = :id;
            """;
    public static final String FIND_ALL_QUERY = """
            SELECT * FROM public.log;
            """;
    public static final String DELETE_ALL_QUERY = """
            DELETE FROM public.log
            WHERE true;
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Log> billRowMapper;

    public LogDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Log> billRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.billRowMapper = billRowMapper;
    }

    @Override
    public Optional<Log> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                billRowMapper

        ).stream().findFirst();
    }

    @Override
    public Optional<Log> findByOrderId(UUID equipmentId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("equipmentId", equipmentId);

        return jdbcTemplate.query(
                FIND_BY_ORDER_ID_QUERY,
                parameterSource,
                billRowMapper
        ).stream().findFirst();
    }

    @Override
    public Log save(Log log) {
        if (nonNull(log.getId())) {
            return update(log);
        }
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(log);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );
        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        log.setId(generatedId);

        return log;
    }

    @Override
    public void delete(Log log) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", log.getId());
        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public List<Log> findAll() {
        return jdbcTemplate.query(
                FIND_ALL_QUERY,
                billRowMapper
        );
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private Log update(Log log) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(log);
        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );
        return log;
    }
}
