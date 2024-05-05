package com.konstantion.adapters.equipment;


import com.google.common.collect.Lists;
import com.konstantion.ApplicationStarter;
import com.konstantion.adapters.gear.GearDatabaseAdapter;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.equipment.Equipment;
import com.konstantion.gear.Gear;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DatabaseTestConfiguration.class, RowMappersConfiguration.class, ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class EquipmentDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    EquipmentDatabaseAdapter orderAdapter;
    GearDatabaseAdapter productAdapter;
    UUID[] PRODUCT_IDS;

    @BeforeEach
    public void setUp() {
        orderAdapter = new EquipmentDatabaseAdapter(jdbcTemplate, rowMappers.orderRowMapper());
        //Initialize related entities for tests
        productAdapter = new GearDatabaseAdapter(jdbcTemplate, rowMappers.productRowMapper());
        Gear first = Gear.builder()
                .name("first")
                .active(true)
                .price(10.0)
                .build();

        Gear second = Gear.builder()
                .name("second")
                .active(true)
                .price(20.0)
                .build();
        productAdapter.save(first);
        productAdapter.save(second);

        PRODUCT_IDS = new UUID[]{first.getId(), second.getId()};
    }

    @AfterEach
    void cleanUp() {
        orderAdapter.deleteAll();
        productAdapter.deleteAll();
    }

    @Test
    void shouldReturnOrderWithIdWhenSaveOrderWithoutId() {
        Equipment equipment = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(List.of(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(now())
                .build();

        orderAdapter.save(equipment);

        assertThat(equipment.getId())
                .isNotNull();
    }

    @Test
    void shouldReturnOrderWhenFindById() {
        Equipment equipment = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(List.of(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(now())
                .build();
        orderAdapter.save(equipment);

        Optional<Equipment> dbOrder = orderAdapter.findById(equipment.getId());

        assertThat(dbOrder).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(equipment.getId())
                        && matched.getGearsId().equals(equipment.getGearsId()));

        assertThat(dbOrder.get().getGearsId())
                .containsExactlyInAnyOrder(PRODUCT_IDS);
    }

    @Test
    void shouldReturnUpdatedOrderWhenSaveOrderWithId() {
        Equipment equipment = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(Lists.newArrayList(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(now())
                .build();
        orderAdapter.save(equipment);

        equipment.setActive(false);
        equipment.getGearsId().remove(PRODUCT_IDS[1]);
        orderAdapter.save(equipment);

        Optional<Equipment> dbOrder = orderAdapter.findById(equipment.getId());

        assertThat(dbOrder).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(equipment.getId())
                        && !matched.isActive()
                        && matched.getGearsId().equals(equipment.getGearsId()));
        assertThat(dbOrder.get().getGearsId())
                .containsExactlyInAnyOrder(PRODUCT_IDS[0]);
    }

    @Test
    void shouldDeleteOrderWhenDeleteOrder() {
        Equipment equipments = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(Lists.newArrayList(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(now())
                .build();
        orderAdapter.save(equipments);

        orderAdapter.delete(equipments);
        List<Equipment> equipment = orderAdapter.findAll();

        assertThat(equipment)
                .isEmpty();
    }

    @Test
    void shouldReturnOrdersWhenFindAll() {
        LocalDateTime createdAt = LocalDateTime.of(2000, 2, 20, 2, 2);
        Equipment first = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(Lists.newArrayList(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(createdAt)
                .build();
        Equipment second = Equipment.builder()
                .id(null)
                .active(true)
                .gearsId(Lists.newArrayList(PRODUCT_IDS[0], PRODUCT_IDS[1]))
                .createdAt(createdAt)
                .build();
        orderAdapter.save(first);
        orderAdapter.save(second);

        List<Equipment> dbEquipments = orderAdapter.findAll();

        assertThat(dbEquipments)
                .containsExactlyInAnyOrder(first, second);
    }
}
