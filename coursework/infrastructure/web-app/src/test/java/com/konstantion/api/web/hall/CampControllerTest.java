package com.konstantion.api.web.hall;

import com.github.javafaker.Faker;
import com.konstantion.camp.Camp;
import com.konstantion.camp.CampPort;
import com.konstantion.dto.hall.converter.HallMapper;
import com.konstantion.dto.hall.dto.HallDto;
import com.konstantion.dto.table.converter.TableMapper;
import com.konstantion.dto.table.dto.TableDto;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.jwt.JwtService;
import com.konstantion.response.ResponseDto;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import com.konstantion.user.Permission;
import com.konstantion.user.Role;
import com.konstantion.user.User;
import com.konstantion.user.UserPort;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.HALL;
import static com.konstantion.utils.EntityNameConstants.HALLS;
import static com.konstantion.utils.EntityNameConstants.TABLES;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class CampControllerTest {
    private static final String API_URL = "/web-api/halls";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ExpeditionPort expeditionPort;
    @Autowired
    private UserPort userPort;
    @Autowired
    private CampPort hallPort;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        User waiter = User.builder()
                .email(faker.internet().emailAddress())
                .active(true)
                .password(passwordEncoder.encode("test"))
                .roles(Role.getWaiterRole())
                .permissions(Permission.getDefaultWaiterPermission())
                .build();
        userPort.save(waiter);
        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        jwtToken = jwtService.generateToken(extraClaim, waiter);
    }

    @AfterEach
    void cleanUp() {
        expeditionPort.deleteAll();
        userPort.deleteAll();
        hallPort.deleteAll();
    }

    @Test
    void shouldReturnForbiddenWhenUnauthorizedUser() {
        webTestClient.get()
                .uri(API_URL)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnHallWhenGetHallById() {
        Camp camp = Camp.builder().active(true).name("hall").build();
        hallPort.save(camp);

        EntityExchangeResult<ResponseDto<HallDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", camp.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<HallDto>>() {
                })
                .returnResult();

        HallDto hallDto = result.getResponseBody()
                .data().get(HALL);

        assertThat(hallDto).isNotNull()
                .isEqualTo(HallMapper.INSTANCE.toDto(camp));
    }

    @Test
    void shouldReturnHallsWhenGetAllActiveHalls() {
        Camp first = Camp.builder().active(true).name("first").build();
        Camp second = Camp.builder().active(false).name("second").build();
        Camp third = Camp.builder().active(true).name("third").build();
        hallPort.save(first);
        hallPort.save(second);
        hallPort.save(third);

        EntityExchangeResult<ResponseDto<List<HallDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<HallDto>>>() {
                })
                .returnResult();

        List<HallDto> halls = result.getResponseBody()
                .data().get(HALLS);

        assertThat(halls)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        HallMapper.INSTANCE.toDto(first),
                        HallMapper.INSTANCE.toDto(third)
                );
    }

    @Test
    void shouldReturnTableHallsWhenGetTablesByHallId() {
        Camp camp = Camp.builder()
                .active(true)
                .name("hall")
                .build();
        hallPort.save(camp);

        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .campId(camp.getId())
                .build();
        expeditionPort.save(table);

        EntityExchangeResult<ResponseDto<List<TableDto>>> result = webTestClient.get()
                .uri(API_URL + "/{id}/tables", camp.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<TableDto>>>() {
                })
                .returnResult();

        List<TableDto> tableDtos = result.getResponseBody()
                .data().get(TABLES);

        assertThat(tableDtos)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        TableMapper.INSTANCE.toDto(table)
                );
    }
}
