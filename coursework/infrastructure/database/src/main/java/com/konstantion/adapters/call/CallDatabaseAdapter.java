package com.konstantion.adapters.call;

import com.konstantion.call.Call;
import com.konstantion.call.CallPort;
import com.konstantion.call.Purpose;
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
public class CallDatabaseAdapter implements CallPort {
    public static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.call
            WHERE id = :id;
            """;
    public static final String FIND_ALL_QUERY = """
            SELECT * FROM public.call;
            """;
    public static final String SAVE_QUERY = """
            INSERT INTO public.call (expedition_id, purpose, opened_at)
            VALUES (:expeditionId, :purpose, :openedAt);
            """;
    public static final String UPDATE_QUERY = """
            UPDATE public.call
               SET expedition_id = :expeditionId,
                   purpose = :purpose,
                   opened_at = :openedAt
            WHERE id = :id;
            """;
    public static final String FIND_WAITER_ID_BY_CALL_ID = """
            SELECT guide_id FROM public.call_guide
            WHERE call_id = :callId;
            """;
    public static final String DELETE_WAITER_ID_BY_CALL_ID = """
            DELETE FROM public.call_guide
            WHERE call_id = :callId;
            """;
    public static final String SAVE_WAITER_ID = """
            INSERT INTO public.call_guide (call_id, guide_id)
            VALUES (:callId, :guideId);
            """;
    public static final String DELETE_QUERY = """
            DELETE FROM public.call
            WHERE id = :id;
            """;
    public static final String FIND_BY_TABLE_ID_AND_PURPOSE_QUERY = """
            SELECT * FROM public.call
            WHERE expedition_id = :expeditionId
            AND purpose = :purpose;
            """;
    public static final String DELETE_ALL_QUERY = """
            DELETE FROM public.call
            WHERE true;
            """;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Call> callRowMapper;

    public CallDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Call> callRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.callRowMapper = callRowMapper;
    }

    @Override
    public List<Call> findAll() {
        List<Call> calls = jdbcTemplate.query(
                FIND_ALL_QUERY,
                callRowMapper
        );

        calls.forEach(call -> call.setGuidesId(fetchWaitersId(call.getId())));

        return calls;
    }

    @Override
    public Optional<Call> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);

        Call call = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                callRowMapper
        ).stream().findFirst().orElse(null);

        if (nonNull(call)) {
            call.setGuidesId(fetchWaitersId(call.getId()));
        }

        return Optional.ofNullable(call);
    }

    @Override
    public Call save(Call call) {
        if (nonNull(call.getId())) {
            return update(call);
        }

        SqlParameterSource parameterSource = getParameterSource(call);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        call.setId(generatedId);

        saveWaiters(call);

        return call;
    }

    @Override
    public void delete(Call call) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", call.getId());
        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public Optional<Call> findByTableIdAndPurpose(UUID expeditionId, Purpose purpose) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("expeditionId", expeditionId)
                .addValue("purpose", purpose.name());

        Call call = jdbcTemplate.query(
                FIND_BY_TABLE_ID_AND_PURPOSE_QUERY,
                parameterSource,
                callRowMapper
        ).stream().findFirst().orElse(null);

        if (nonNull(call)) {
            call.setGuidesId(fetchWaitersId(call.getId()));
        }

        return Optional.ofNullable(call);
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private Call update(Call call) {
        SqlParameterSource parameterSource = getParameterSource(call);

        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );

        updateWaitersId(call);

        return call;
    }

    private Set<UUID> fetchWaitersId(UUID callId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", callId);
        return new HashSet<>(jdbcTemplate.queryForList(
                FIND_WAITER_ID_BY_CALL_ID,
                parameterSource,
                UUID.class
        ));
    }

    private void updateWaitersId(Call call) {
        deleteWaitersByCallId(call.getId());
        saveWaiters(call);
    }

    private void deleteWaitersByCallId(UUID callId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", callId);
        jdbcTemplate.update(
                DELETE_WAITER_ID_BY_CALL_ID,
                parameterSource
        );
    }

    private void saveWaiters(Call call) {
        call.getGuidesId().forEach(id -> saveWaiterId(call.getId(), id));
    }

    private void saveWaiterId(UUID callId, UUID guideId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", callId)
                .addValue("guideId", guideId);
        jdbcTemplate.update(
                SAVE_WAITER_ID,
                parameterSource
        );
    }

    private SqlParameterSource getParameterSource(Call call) {
        return new BeanPropertySqlParameterSource(call) {
            @Override
            public int getSqlType(String paramName) {
                if ("purpose".equals(paramName)) {
                    return Types.VARCHAR;
                }
                return super.getSqlType(paramName);
            }
        };
    }
}
