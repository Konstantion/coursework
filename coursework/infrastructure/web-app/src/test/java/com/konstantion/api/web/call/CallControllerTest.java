package com.konstantion.api.web.call;

import com.github.javafaker.Faker;
import com.google.common.collect.Sets;
import com.konstantion.call.Call;
import com.konstantion.call.CallPort;
import com.konstantion.call.Purpose;
import com.konstantion.dto.call.converter.CallMapper;
import com.konstantion.dto.call.dto.CallDto;
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
import java.util.Optional;

import static com.konstantion.utils.EntityNameConstants.CALLS;
import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class CallControllerTest {
    private static final String API_URL = "/web-api/calls";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CallPort callPort;
    @Autowired
    private UserPort userPort;
    @Autowired
    private ExpeditionPort expeditionPort;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    private String jwtToken;
    private User waiter;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        waiter = User.builder()
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
        callPort.deleteAll();
        userPort.deleteAll();
        expeditionPort.deleteAll();
    }

    @Test
    void shouldReturnUserCallsWhenGetCallsByUser() {
        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .build();
        expeditionPort.save(table);

        Call callWaiter = Call.builder()
                .purpose(Purpose.CALL_GUIDE)
                .expeditionId(table.getId())
                .guidesId(Sets.newHashSet(waiter.getId()))
                .build();
        Call callBill = Call.builder()
                .purpose(Purpose.CALL_BILL)
                .expeditionId(table.getId())
                .guidesId(Sets.newHashSet(waiter.getId()))
                .build();
        callPort.save(callWaiter);
        callPort.save(callBill);

        EntityExchangeResult<ResponseDto<List<CallDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<CallDto>>>() {
                })
                .returnResult();

        List<CallDto> callDtos = result.getResponseBody()
                .data().get(CALLS);

        assertThat(callDtos)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        CallMapper.INSTANCE.toDto(callWaiter),
                        CallMapper.INSTANCE.toDto(callBill)
                );
    }

    @Test
    void shouldCloseCallWhenCloseCall() {
        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .build();
        expeditionPort.save(table);

        Call callWaiter = Call.builder()
                .purpose(Purpose.CALL_GUIDE)
                .expeditionId(table.getId())
                .guidesId(Sets.newHashSet(waiter.getId()))
                .build();
        callPort.save(callWaiter);

        webTestClient.delete()
                .uri(API_URL + "/{id}", callWaiter.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk();

        Optional<Call> dbCall = callPort.findById(callWaiter.getId());
        assertThat(dbCall).isNotPresent();
    }

    @Test
    void shouldReturnBadRequestWhenCloseCallWithUserNotRelatedToCall() {
        User user = User.builder()
                .email("email")
                .active(true)
                .password(passwordEncoder.encode("password"))
                .permissions(Permission.getDefaultWaiterPermission())
                .roles(Role.getWaiterRole())
                .build();
        userPort.save(user);

        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .build();
        expeditionPort.save(table);

        Call callWaiter = Call.builder()
                .purpose(Purpose.CALL_GUIDE)
                .expeditionId(table.getId())
                .guidesId(Sets.newHashSet(user.getId()))
                .build();
        callPort.save(callWaiter);

        webTestClient.delete()
                .uri(API_URL + "/{id}", callWaiter.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest();

        Optional<Call> dbCall = callPort.findById(callWaiter.getId());
        assertThat(dbCall).isPresent();
    }

    @Test
    void shouldCloseCallWhenCloseCallWithUserAdmin() {
        User user = User.builder()
                .email("email")
                .active(true)
                .password(passwordEncoder.encode("password"))
                .permissions(Permission.getDefaultAdminPermission())
                .roles(Role.getAdminRole())
                .build();
        userPort.save(user);
        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        String adminJwt = jwtService.generateToken(extraClaim, user);

        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .build();
        expeditionPort.save(table);

        Call callWaiter = Call.builder()
                .purpose(Purpose.CALL_GUIDE)
                .expeditionId(table.getId())
                .guidesId(Sets.newHashSet(waiter.getId()))
                .build();
        callPort.save(callWaiter);

        webTestClient.delete()
                .uri(API_URL + "/{id}", callWaiter.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", adminJwt))
                .exchange()
                .expectStatus().isOk();

        Optional<Call> dbCall = callPort.findById(callWaiter.getId());
        assertThat(dbCall).isNotPresent();
    }
}
