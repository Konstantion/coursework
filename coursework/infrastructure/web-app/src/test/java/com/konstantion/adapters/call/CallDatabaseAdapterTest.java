package com.konstantion.adapters.call;

import com.konstantion.ApplicationStarter;
import com.konstantion.adapters.expedition.ExpeditionDatabaseAdapter;
import com.konstantion.adapters.user.UserDatabaseAdapter;
import com.konstantion.call.Call;
import com.konstantion.call.Purpose;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.expedition.Expedition;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {
        DatabaseTestConfiguration.class,
        RowMappersConfiguration.class,
        ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class CallDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    CallDatabaseAdapter callAdapter;
    UserDatabaseAdapter userDatabaseAdapter;
    ExpeditionDatabaseAdapter tableDatabaseAdapter;
    UUID[] USER_IDS;
    UUID[] TABLE_IDS;

    @BeforeEach
    public void setUp() {
        callAdapter = new CallDatabaseAdapter(jdbcTemplate, rowMappers.callRowMapper());
        //Initialize related entities for tests
        userDatabaseAdapter = new UserDatabaseAdapter(jdbcTemplate, rowMappers.userRowMapper());
        tableDatabaseAdapter = new ExpeditionDatabaseAdapter(jdbcTemplate, rowMappers.tableRowMapper());
        Expedition table = Expedition.builder()
                .name("TEST_TABLE")
                .active(true)
                .password("PASSWORD")
                .build();
        User user = User.builder()
                .email("TEST_EMAIL")
                .active(true)
                .password("PASSWORD")
                .build();
        tableDatabaseAdapter.save(table);
        userDatabaseAdapter.save(user);
        USER_IDS = new UUID[]{user.getId()};
        TABLE_IDS = new UUID[]{table.getId()};
    }

    @AfterEach
    void cleanUp() {
        callAdapter.deleteAll();
        userDatabaseAdapter.deleteAll();
        tableDatabaseAdapter.deleteAll();
    }

    @Test
    void shouldReturnCallWithIdWhenSaveCallWithoutId() {
        Call call = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        callAdapter.save(call);

        assertThat(call.getId()).isNotNull();
    }

    @Test
    void shouldUpdateCallWhenSaveCallWithId() {
        LocalDateTime openedAt = now();
        Call call = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .openedAt(openedAt)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        callAdapter.save(call);
        UUID id = call.getId();

        call.setPurpose(Purpose.CALL_GUIDE);
        callAdapter.save(call);

        Optional<Call> dbCall = callAdapter.findById(id);

        assertThat(dbCall).isPresent()
                .get()
                .matches(matchedCall -> matchedCall.getId().equals(id)
                        && matchedCall.getPurpose().equals(Purpose.CALL_GUIDE));
        assertThat(dbCall.get().getOpenedAt())
                .isEqualToIgnoringSeconds(openedAt);


    }

    @Test
    void shouldReturnCallsWhenFindAll() {
        Call first = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        Call second = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        callAdapter.save(first);
        callAdapter.save(second);

        List<Call> dbCalls = callAdapter.findAll();

        assertThat(dbCalls)
                .hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldDeleteCallWhenDeleteCall() {
        Call first = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        Call second = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        callAdapter.save(first);
        callAdapter.save(second);

        callAdapter.delete(first);
        List<Call> dbCalls = callAdapter.findAll();

        assertThat(dbCalls)
                .hasSize(1)
                .containsExactlyInAnyOrder(second);
    }

    @Test
    void shouldReturnCallWhenFindByTableIdAndPurpose() {
        Call first = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_BILL)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        Call second = Call.builder()
                .id(null)
                .purpose(Purpose.CALL_GUIDE)
                .expeditionId(TABLE_IDS[0])
                .guidesId(Set.of(USER_IDS[0]))
                .build();
        callAdapter.save(first);
        callAdapter.save(second);

        Optional<Call> dbCall = callAdapter.findByTableIdAndPurpose(TABLE_IDS[0], Purpose.CALL_GUIDE);


        assertThat(dbCall).isPresent()
                .get()
                .isEqualTo(second);
    }
}
