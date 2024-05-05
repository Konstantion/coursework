package com.konstantion.adapters.camp;

import com.konstantion.ApplicationStarter;
import com.konstantion.camp.Camp;
import com.konstantion.configuration.RowMappersConfiguration;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DatabaseTestConfiguration.class, RowMappersConfiguration.class, ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class CampDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    CampDatabaseAdapter hallAdapter;

    @BeforeEach
    public void setUp() {
        hallAdapter = new CampDatabaseAdapter(jdbcTemplate, rowMappers.hallRowMapper());
        hallAdapter.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        hallAdapter.deleteAll();
    }

    @Test
    void shouldReturnHallWithIdWhenSaveHallWithoutId() {
        Camp camp = Camp.builder()
                .active(true)
                .id(null)
                .name("test")
                .build();
        hallAdapter.save(camp);

        assertThat(camp.getId()).isNotNull();
    }

    @Test
    void shouldUpdateHallWhenSaveHallWithId() {
        Camp camp = Camp.builder().active(true)
                .active(true)
                .id(null)
                .name("test")
                .build();
        hallAdapter.save(camp);
        UUID id = camp.getId();

        camp.setName("newName");
        hallAdapter.save(camp);

        Optional<Camp> dbHall = hallAdapter.findById(id);

        assertThat(dbHall).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(id)
                        && matched.getName().equals("newName"));
    }

    @Test
    void shouldReturnHallsWhenFindAll() {
        Camp first = Camp.builder()
                .active(true)
                .id(null)
                .name("first")
                .build();
        Camp second = Camp.builder()
                .active(true)
                .id(null)
                .name("second")
                .build();
        hallAdapter.save(first);
        hallAdapter.save(second);

        List<Camp> dbCamp = hallAdapter.findAll();

        assertThat(dbCamp)
                .hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldDeleteHallWhenDeleteHall() {
        Camp first = Camp.builder()
                .active(true)
                .id(null)
                .name("first")
                .build();
        Camp second = Camp.builder()
                .active(true)
                .id(null)
                .name("second")
                .build();
        hallAdapter.save(first);
        hallAdapter.save(second);

        hallAdapter.delete(first);
        List<Camp> dbCamps = hallAdapter.findAll();

        assertThat(dbCamps)
                .hasSize(1)
                .containsExactlyInAnyOrder(second);
    }
}
