package com.konstantion.adapters.log;

import com.konstantion.ApplicationStarter;
import com.konstantion.adapters.equipment.EquipmentDatabaseAdapter;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.equipment.Equipment;
import com.konstantion.log.Log;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
class LogDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();

    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMapper<Log> billRowMapper;

    @Autowired
    RowMapper<Equipment> orderRowMapper;

    LogDatabaseAdapter billAdapter;
    EquipmentDatabaseAdapter orderDatabaseAdapter;
    UUID[] ORDER_IDS;

    @BeforeEach
    void setUp() {
        billAdapter = new LogDatabaseAdapter(jdbcTemplate, billRowMapper);
        //Initialize related entities for tests
        orderDatabaseAdapter = new EquipmentDatabaseAdapter(jdbcTemplate, orderRowMapper);
        Equipment first = Equipment.builder()
                .createdAt(now())
                .active(true)
                .build();
        Equipment second = Equipment.builder()
                .createdAt(now())
                .active(true)
                .build();
        orderDatabaseAdapter.save(first);
        orderDatabaseAdapter.save(second);

        ORDER_IDS = new UUID[]{first.getId(), second.getId()};
    }

    @AfterEach
    void cleanUp() {
        billAdapter.deleteAll();
        orderDatabaseAdapter.deleteAll();
    }

    @Test
    void shouldReturnBillWithIdWhenSaveBillWithoutId() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();
        billAdapter.save(log);

        assertThat(log).isNotNull()
                .extracting(Log::getId).isNotNull();
    }

    @Test
    void shouldReturnSavedBillFindById() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();
        billAdapter.save(log);

        Optional<Log> dbBill = billAdapter.findById(log.getId());

        assertThat(dbBill).isPresent()
                .get().isEqualTo(log);
    }

    @Test
    void shouldReturnAllBillsWhenGetAll() {
        Log first = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();
        Log second = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[1])
                .active(true)
                .price(23.00)
                .priceWithDiscount(10.00)
                .build();

        billAdapter.save(first);
        billAdapter.save(second);

        List<Log> dbLogs = billAdapter.findAll();

        assertThat(dbLogs)
                .hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldDeleteBillWhenDelete() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();

        billAdapter.save(log);
        billAdapter.delete(log);
        List<Log> dbLogs = billAdapter.findAll();

        assertThat(dbLogs).isEmpty();
    }

    @Test
    void shouldReturnOptionalWithBillWhenFindByOrderIdWithExistingOrderId() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();

        billAdapter.save(log);
        Optional<Log> dbBill = billAdapter.findByOrderId(ORDER_IDS[0]);

        assertThat(dbBill).isPresent()
                .get().isEqualTo(log);
    }

    @Test
    void shouldReturnOptionalEmptyWhenFindByOrderIdWithNonExistingOrderId() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();

        billAdapter.save(log);
        Optional<Log> dbBill = billAdapter.findByOrderId(ORDER_IDS[1]);

        assertThat(dbBill).isNotPresent();
    }

    @Test
    void shouldUpdateBillWhenSaveBillWithBillThatExists() {
        Log log = Log.builder()
                .id(null)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(10.00)
                .priceWithDiscount(10.00)
                .build();
        billAdapter.save(log);

        UUID dbId = log.getId();
        Log newLog = Log.builder()
                .id(dbId)
                .equipmentId(ORDER_IDS[0])
                .active(true)
                .price(123.00)
                .priceWithDiscount(10.00)
                .build();
        billAdapter.save(newLog);

        Optional<Log> dbBill = billAdapter.findById(dbId);

        assertThat(dbBill).isPresent()
                .get()
                .matches(matched -> matched.getPrice().equals(123.00)
                        && matched.getId().equals(log.getId()));
    }
}
