package com.konstantion.adapters.camp;

import com.konstantion.camp.Camp;
import com.konstantion.camp.CampPort;
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
public class CampDatabaseAdapter implements CampPort {
    public static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.camp
            WHERE id = :id;
            """;
    public static final String SAVE_QUERY = """
            INSERT INTO public.camp (name, created_at, active)
            VALUES (:name, :createdAt, :active);
            """;
    public static final String UPDATE_QUERY = """
            UPDATE public.camp
               SET name = :name,
                   created_at = :createdAt,
                   active = :active
               WHERE id = :id;
            """;
    public static final String DELETE_QUERY = """
            DELETE FROM public.camp
            WHERE id = :id;
            """;
    public static final String FIND_ALL_QUERY = """
            SELECT * FROM public.camp;
            """;
    public static final String DELETE_ALL_QUERY = """
            DELETE FROM public.camp
            WHERE true;
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Camp> hallRowMapper;

    public CampDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Camp> hallRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.hallRowMapper = hallRowMapper;
    }

    @Override
    public List<Camp> findAll() {
        return jdbcTemplate.query(
                FIND_ALL_QUERY,
                hallRowMapper
        );
    }

    @Override
    public Optional<Camp> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                hallRowMapper
        ).stream().findFirst();
    }

    @Override
    public Camp save(Camp camp) {
        if (nonNull(camp.getId())) {
            return update(camp);
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(camp);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        camp.setId(generatedId);

        return camp;
    }

    @Override
    public void delete(Camp camp) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", camp.getId());
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

    private Camp update(Camp camp) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(camp);
        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );
        return camp;
    }
}
