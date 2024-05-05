package com.konstantion.adapters.equipment;

import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
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
public class EquipmentDatabaseAdapter implements EquipmentPort {
    private static final String FIND_BY_ID_QUERY = """
            SELECT * FROM public.equipment
            WHERE id = :id;
            """;
    private static final String FIND_PRODUCTS_BY_ORDER_ID = """
            SELECT gear_id FROM public.equipment_gear
            WHERE equipment_id = :equipmentId;
            """;
    private static final String DELETE_QUERY = """
            DELETE FROM public.equipment
            WHERE id = :id;
            """;
    private static final String UPDATE_QUERY = """
            UPDATE public.equipment
            SET expedition_id = :expeditionId,
                user_id = :userId,
                log_id = :logId,
                created_at = :createdAt,
                closed_at = :closedAt,
                active = :active
            WHERE id = :id;
            """;
    private static final String DELETE_PRODUCTS_QUERY = """
            DELETE FROM public.equipment_gear
            WHERE equipment_id = :equipmentId;
            """;
    private static final String SAVE_PRODUCT_QUERY = """
            INSERT INTO public.equipment_gear (equipment_id, gear_id)
            VALUES (:equipmentId, :gearId);
            """;
    private static final String SAVE_QUERY = """
            INSERT INTO public.equipment (expedition_id, user_id, log_id, created_at, closed_at, active)
            VALUES (:expeditionId, :userId, :logId, :createdAt, :closedAt, :active);
            """;
    private static final String FIND_ALL_QUERY = """
            SELECT * FROM public.equipment;
            """;
    private static final String DELETE_ALL_QUERY = """
            DELETE FROM public.equipment
            WHERE true;
            """;
    final NamedParameterJdbcTemplate jdbcTemplate;
    final RowMapper<Equipment> orderRowMapper;

    public EquipmentDatabaseAdapter(NamedParameterJdbcTemplate jdbcTemplate, RowMapper<Equipment> orderRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.orderRowMapper = orderRowMapper;
    }

    @Override
    public Equipment save(Equipment equipment) {
        if (nonNull(equipment.getId())) {
            return update(equipment);
        }

        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(equipment);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                SAVE_QUERY,
                parameterSource,
                keyHolder
        );

        UUID generatedId = (UUID) Objects.requireNonNull(keyHolder.getKeys()).get("id");
        equipment.setId(generatedId);

        saveOrderProducts(equipment.getId(), equipment.getGearsId());

        return equipment;
    }

    @Override
    public Optional<Equipment> findById(UUID id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id);

        Equipment equipment = jdbcTemplate.query(
                FIND_BY_ID_QUERY,
                parameterSource,
                orderRowMapper
        ).stream().findFirst().orElse(null);

        if (nonNull(equipment)) {
            equipment.setGearsId(findProductsByOrderId(equipment.getId()));
        }

        return Optional.ofNullable(equipment);
    }

    @Override
    public void delete(Equipment equipment) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", equipment.getId());

        jdbcTemplate.update(
                DELETE_QUERY,
                parameterSource
        );
    }

    @Override
    public List<Equipment> findAll() {
        List<Equipment> equipments = jdbcTemplate.query(
                FIND_ALL_QUERY,
                orderRowMapper
        );

        for (Equipment equipment : equipments) {
            equipment.setGearsId(findProductsByOrderId(equipment.getId()));
        }

        return equipments;
    }

    @Override
    public void deleteAll() {
        jdbcTemplate.update(
                DELETE_ALL_QUERY,
                new MapSqlParameterSource()
        );
    }

    private List<UUID> findProductsByOrderId(UUID equipmentId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("equipmentId", equipmentId);

        return jdbcTemplate.queryForList(
                FIND_PRODUCTS_BY_ORDER_ID,
                parameterSource,
                UUID.class
        );
    }

    private Equipment update(Equipment equipment) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(equipment);
        jdbcTemplate.update(
                UPDATE_QUERY,
                parameterSource
        );

        updateOrderProducts(equipment);

        return equipment;
    }

    private void updateOrderProducts(Equipment equipment) {
        deleteOrderProducts(equipment.getId());
        saveOrderProducts(equipment.getId(), equipment.getGearsId());
    }

    private void deleteOrderProducts(UUID equipmentId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("equipmentId", equipmentId);
        jdbcTemplate.update(
                DELETE_PRODUCTS_QUERY,
                parameterSource
        );
    }

    private void saveOrderProducts(UUID equipmentId, List<UUID> productsId) {
        productsId.forEach(gearId -> {
            SqlParameterSource parameterSource = new MapSqlParameterSource()
                    .addValue("equipmentId", equipmentId)
                    .addValue("gearId", gearId);
            jdbcTemplate.update(
                    SAVE_PRODUCT_QUERY,
                    parameterSource
            );
        });
    }
}
