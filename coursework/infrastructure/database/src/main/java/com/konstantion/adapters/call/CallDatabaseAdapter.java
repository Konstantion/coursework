package com.konstantion.adapters.call;

import com.konstantion.call.Call;
import com.konstantion.call.CallPort;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;

@Component
public record CallDatabaseAdapter(
        NamedParameterJdbcTemplate jdbcTemplate,
        RowMapper<Call> callRowMapper
) implements CallPort {
    public static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.call
            WHERE id = :id;
            """;


    public static final String SAVE_QUERY = """
            INSERT INTO public.call (table_id, purpose, opened_at, closed_at, active)
            VALUES (:tableId, :purpose, :openedAt, :closedAt, :active);
            """;

    public static final String UPDATE_QUERY = """
            UPDATE public.call
               SET table_id = :tableId,
                   purpose = :purpose,
                   opened_at = :openedAt,
                   closed_at = :closedAt,
                   active = :active
            WHERE id = :id;
            """;
    public static final String FIND_WAITER_ID_BY_CALL_ID = """
            SELECT waiter_id FROM public.call_waiter
            WHERE call_id = :callId;
            """;

    public static final String DELETE_WAITER_ID_BY_CALL_ID = """
            DELETE FROM public.call_waiter
            WHERE call_id = :callId;
            """;

    public static final String SAVE_WAITER_ID = """
            INSERT INTO public.call_waiter (call_id, waiter_id)
            VALUES (:callId, :waiterId);
            """;

    public static final String DELETE_QUERY = """
            DELETE FROM public.call
            WHERE id = :id;
            """;

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
            call.setWaitersId(fetchWaitersId(call.getId()));
        }

        return Optional.ofNullable(call);
    }

    @Override
    public Call save(Call call) {
        if (nonNull(call.getId())) {
            return update(call);
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(call);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        call.setId(generatedId);

        updateWaitersId(call);
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

    private Call update(Call call) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(call);
        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );

        updateWaitersId(call);

        return call;
    }

    private List<UUID> fetchWaitersId(UUID callId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", callId);
        return jdbcTemplate.queryForList(
                FIND_WAITER_ID_BY_CALL_ID,
                parameterSource,
                UUID.class
        );
    }

    private void updateWaitersId(Call call) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", call.getId());
        jdbcTemplate.update(
                DELETE_WAITER_ID_BY_CALL_ID,
                parameterSource
        );

        call.getWaitersId().forEach(id -> saveWaiterId(call.getId(), id));
    }

    private void saveWaiterId(UUID callId, UUID waiterId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("callId", callId)
                .addValue("waiterId", waiterId);
        jdbcTemplate.update(
                SAVE_WAITER_ID,
                parameterSource
        );
    }
}
