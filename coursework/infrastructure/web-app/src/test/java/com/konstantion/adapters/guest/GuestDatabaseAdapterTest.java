package com.konstantion.adapters.guest;

import com.konstantion.ApplicationStarter;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.guest.Guest;
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
class GuestDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    GuestDatabaseAdapter guestAdapter;

    @BeforeEach
    public void setUp() {
        guestAdapter = new GuestDatabaseAdapter(jdbcTemplate, rowMappers.guestRowMapper());
        guestAdapter.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        guestAdapter.deleteAll();
    }

    @Test
    void shouldReturnGuestWhenFindGuestByName() {
        Guest guest = Guest.builder()
                .active(true)
                .id(null)
                .name("test")
                .build();
        guestAdapter.save(guest);

        Optional<Guest> dbGuest = guestAdapter.findByName("test");

        assertThat(dbGuest).isPresent()
                .get()
                .isEqualTo(guest);
    }

    @Test
    void shouldReturnGuestWithIdWhenSaveGuestWithoutId() {
        Guest guest = Guest.builder()
                .active(true)
                .id(null)
                .name("test")
                .build();
        guestAdapter.save(guest);

        assertThat(guest.getId()).isNotNull();
    }

    @Test
    void shouldUpdateGuestWhenSaveGuestWithId() {
        Guest guest = Guest.builder().active(true)
                .active(true)
                .id(null)
                .name("test")
                .build();
        guestAdapter.save(guest);
        UUID id = guest.getId();

        guest.setName("newName");
        guestAdapter.save(guest);

        Optional<Guest> dbGuest = guestAdapter.findById(id);

        assertThat(dbGuest).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(id)
                        && matched.getName().equals("newName"));
    }

    @Test
    void shouldReturnGuestsWhenFindAll() {
        Guest first = Guest.builder()
                .active(true)
                .id(null)
                .name("first")
                .build();
        Guest second = Guest.builder()
                .active(true)
                .id(null)
                .name("second")
                .build();
        guestAdapter.save(first);
        guestAdapter.save(second);

        List<Guest> dbGuest = guestAdapter.findAll();

        assertThat(dbGuest)
                .hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldDeleteGuestWhenDeleteGuest() {
        Guest first = Guest.builder()
                .active(true)
                .id(null)
                .name("first")
                .build();
        Guest second = Guest.builder()
                .active(true)
                .id(null)
                .name("second")
                .build();
        guestAdapter.save(first);
        guestAdapter.save(second);

        guestAdapter.delete(first);
        List<Guest> dbGuest = guestAdapter.findAll();

        assertThat(dbGuest)
                .hasSize(1)
                .containsExactlyInAnyOrder(second);
    }
}
