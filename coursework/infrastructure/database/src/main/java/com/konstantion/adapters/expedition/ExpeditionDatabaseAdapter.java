package com.konstantion.adapters.expedition;

import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Repository
public class ExpeditionDatabaseAdapter implements ExpeditionPort {
    private static final String FIND_ALL_QUERY = """
            SELECT * FROM public.expedition;
            """;
    private static final String DELETE_ALL_QUERY = """
            DELETE  FROM public.expedition
            WHERE true;
            """;
    private static final String FIND_ALL_WHERE_HALL_ID_QUERY = """
            SELECT * FROM public.expedition
            WHERE camp_id = :campId;
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.expedition
            WHERE id = :id;
            """;
    private static final String FIND_BY_NAME_QUERY = """
            SELECT * FROM public.expedition
            WHERE name = :name;
            """;
    private static final String DELETE_QUERY = """
            DELETE FROM public.expedition
            WHERE id = :id;
            """;
    private static final String SAVE_QUERY = """
            INSERT INTO public.expedition (name, capacity, expedition_type, camp_id, equipment_id, created_at, deleted_at, active, password)
            VALUES (:name, :capacity, :expeditionType, :campId, :equipmentId, :createdAt, :deletedAt, :active, :password);
            """;
    private static final String UPDATE_QUERY = """
            UPDATE public.expedition
            SET name = :name,
                capacity = :capacity,
                expedition_type = :expeditionType,
                camp_id = :campId,
                equipment_id = :equipmentId,
                active = :active,
                password = :password,
                created_at = :createdAt,
                deleted_at = :deletedAt
            WHERE id = :id;
            """;
    private static final String FIND_WAITERS_BY_TABLE_ID = """
            SELECT guide_id FROM public.expedition_guide
            WHERE expedition_id = :expeditionId;
            """;
    private static final String DELETE_WAITERS_QUERY = """
            DELETE FROM public.expedition_guide
            WHERE expedition_id = :expeditionId;
            """;
    private static final String SAVE_WAITER_QUERY = """
            INSERT INTO public.expedition_guide (expedition_id, guide_id)
            VALUES (:expeditionId, :guideId);
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Expedition> tableRowMapper;

    public ExpeditionDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Expedition> tableRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableRowMapper = tableRowMapper;
    }

    @Override
    public Expedition save(Expedition table) {
        if (nonNull(table.getId())) {
            return update(table);
        }

        SqlParameterSource parameterSource = getParameterSource(table);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        table.setId(generatedId);

        updateTableWaiters(table);

        return table;
    }

    @Override
    public void delete(Expedition table) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", table.getId());
        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public Optional<Expedition> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);
        Expedition table = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                tableRowMapper
        ).stream().findFirst().orElse(null);

        if (nonNull(table)) {
            table.setGuidesId(findWaitersByTableId(table.getId()));
        }

        return Optional.ofNullable(table);
    }

    @Override
    public List<Expedition> findAll() {
        List<Expedition> tables = jdbcTemplate.query(
                FIND_ALL_QUERY,
                tableRowMapper
        );

        for (Expedition table : tables) {
            table.setGuidesId(findWaitersByTableId(table.getId()));
        }

        return tables;
    }

    @Override
    public List<Expedition> findAllWhereHallId(UUID campId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("campId", campId);
        List<Expedition> tables = jdbcTemplate.query(
                FIND_ALL_WHERE_HALL_ID_QUERY,
                parameterSource,
                tableRowMapper
        );

        for (Expedition table : tables) {
            table.setGuidesId(findWaitersByTableId(table.getId()));
        }
        return tables;
    }

    @Override
    public Optional<Expedition> findByName(String name) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", name);
        Expedition table = jdbcTemplate.query(
                FIND_BY_NAME_QUERY,
                parameterSource,
                tableRowMapper
        ).stream().findFirst().orElse(null);

        if (nonNull(table)) {
            table.setGuidesId(findWaitersByTableId(table.getId()));
        }

        return Optional.ofNullable(table);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private Set<UUID> findWaitersByTableId(UUID expeditionId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("expeditionId", expeditionId);
        return new HashSet<>(jdbcTemplate.queryForList(
                FIND_WAITERS_BY_TABLE_ID,
                parameterSource,
                UUID.class
        ));
    }

    private Expedition update(Expedition table) {
        SqlParameterSource parameterSource = getParameterSource(table);
        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );

        updateTableWaiters(table);

        return table;
    }

    private void updateTableWaiters(Expedition table) {
        deleteTableWaiters(table.getId());
        saveTableWaiters(table.getId(), table.getGuidesId());
    }

    private void deleteTableWaiters(UUID expeditionId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("expeditionId", expeditionId);
        jdbcTemplate.update(
                DELETE_WAITERS_QUERY,
                parameterSource
        );
    }

    private void saveTableWaiters(UUID expeditionId, Set<UUID> waitersId) {
        if (waitersId == null) return;

        waitersId.forEach(guideId -> {
            SqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("expeditionId", expeditionId)
                    .addValue("guideId", guideId);
            jdbcTemplate.update(
                    SAVE_WAITER_QUERY,
                    parameterSource
            );
        });
    }

    private SqlParameterSource getParameterSource(Expedition table) {
        return new BeanPropertySqlParameterSource(table) {
            @Override
            public int getSqlType(String paramName) {
                if ("expeditionType".equals(paramName)) {
                    return Types.VARCHAR;
                }
                return super.getSqlType(paramName);
            }
        };
    }
}
