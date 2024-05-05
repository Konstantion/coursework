package com.konstantion.adapters.expedition;

import com.konstantion.ApplicationStarter;
import com.konstantion.adapters.camp.CampDatabaseAdapter;
import com.konstantion.adapters.user.UserDatabaseAdapter;
import com.konstantion.camp.Camp;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionType;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import com.konstantion.user.Permission;
import com.konstantion.user.Role;
import com.konstantion.user.User;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DatabaseTestConfiguration.class, RowMappersConfiguration.class, ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class TableDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    ExpeditionDatabaseAdapter tableAdapter;
    CampDatabaseAdapter hallAdapter;
    UserDatabaseAdapter userAdapter;

    UUID[] HALL_IDS;
    UUID[] USER_IDS;

    @BeforeEach
    void setUp() {
        tableAdapter = new ExpeditionDatabaseAdapter(jdbcTemplate, rowMappers.tableRowMapper());
        //Initialize related entities for tests
        hallAdapter = new CampDatabaseAdapter(jdbcTemplate, rowMappers.hallRowMapper());
        userAdapter = new UserDatabaseAdapter(jdbcTemplate, rowMappers.userRowMapper());
        Camp camp = Camp.builder()
                .name("test")
                .active(true)
                .build();
        hallAdapter.save(camp);

        User waiter = User.builder()
                .active(true)
                .email("email")
                .password("password")
                .firstName("name")
                .lastName("eman")
                .roles(Role.getWaiterRole())
                .permissions(Permission.getDefaultWaiterPermission())
                .build();
        userAdapter.save(waiter);

        HALL_IDS = new UUID[]{camp.getId()};
        USER_IDS = new UUID[]{waiter.getId()};
    }

    @AfterEach
    void cleanUp() {
        tableAdapter.deleteAll();
        hallAdapter.deleteAll();
        userAdapter.deleteAll();
    }

    @Test
    void shouldReturnTableWithIdWhenSaveTableWithoutId() {
        Expedition table = Expedition.builder()
                .name("table")
                .expeditionType(ExpeditionType.COMMON)
                .password("test")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        tableAdapter.save(table);

        assertThat(table.getId())
                .isNotNull();
    }

    @Test
    void shouldReturnTableWhenFindById() {
        Set<UUID> waitersId = Set.of(USER_IDS[0]);
        Expedition table = Expedition.builder()
                .name("table")
                .expeditionType(ExpeditionType.COMMON)
                .password("test")
                .campId(HALL_IDS[0])
                .guidesId(waitersId)
                .active(true)
                .build();
        tableAdapter.save(table);

        Optional<Expedition> dbTable = tableAdapter.findById(table.getId());

        assertThat(dbTable).isPresent()
                .get()
                .matches(matched -> matched.getGuidesId().equals(waitersId));
    }

    @Test
    void shouldReturnUpdatedTableWhenSaveTableWithId() {
        Expedition table = Expedition.builder()
                .name("table")
                .expeditionType(ExpeditionType.COMMON)
                .password("test")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        tableAdapter.save(table);

        UUID tableId = table.getId();
        table.setName("newName");
        tableAdapter.save(table);
        Optional<Expedition> dbTable = tableAdapter.findById(tableId);

        assertThat(dbTable).isPresent()
                .get()
                .extracting(Expedition::getName).isEqualTo("newName");
    }

    @Test
    void shouldDeleteTableWhenDelete() {
        Expedition first = Expedition.builder()
                .name("first")
                .expeditionType(ExpeditionType.COMMON)
                .password("first")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        Expedition second = Expedition.builder()
                .name("second")
                .expeditionType(ExpeditionType.COMMON)
                .password("second")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        tableAdapter.save(first);
        tableAdapter.save(second);

        tableAdapter.delete(first);

        List<Expedition> dbTabled = tableAdapter.findAll();

        assertThat(dbTabled)
                .containsExactlyInAnyOrder(second);
    }

    @Test
    void shouldReturnAllTablesWhenGetAll() {
        Expedition first = Expedition.builder()
                .name("first")
                .expeditionType(ExpeditionType.COMMON)
                .password("first")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        Expedition second = Expedition.builder()
                .name("second")
                .expeditionType(ExpeditionType.COMMON)
                .password("second")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        tableAdapter.save(first);
        tableAdapter.save(second);

        List<Expedition> dbTables = tableAdapter.findAll();

        assertThat(dbTables)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldReturnTableWhenFindByName() {
        Expedition table = Expedition.builder()
                .name("table")
                .expeditionType(ExpeditionType.COMMON)
                .password("test")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        tableAdapter.save(table);

        Optional<Expedition> dbTable = tableAdapter.findByName("table");

        assertThat(dbTable).isPresent()
                .get()
                .isEqualTo(table);
    }

    @Test
    void shouldReturnTablesWithHallIdWhenFindWhereHallId() {
        Expedition first = Expedition.builder()
                .name("first")
                .expeditionType(ExpeditionType.COMMON)
                .password("first")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        Expedition second = Expedition.builder()
                .name("second")
                .expeditionType(ExpeditionType.COMMON)
                .password("second")
                .campId(HALL_IDS[0])
                .active(true)
                .build();
        Expedition third = Expedition.builder()
                .name("third")
                .expeditionType(ExpeditionType.COMMON)
                .password("third")
                .active(true)
                .build();
        tableAdapter.save(first);
        tableAdapter.save(second);
        tableAdapter.save(third);

        List<Expedition> dbTables = tableAdapter.findAllWhereHallId(HALL_IDS[0]);

        assertThat(dbTables).hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }
}
