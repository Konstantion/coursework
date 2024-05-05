package com.konstantion.api.web.guest;

import com.github.javafaker.Faker;
import com.konstantion.dto.guest.converter.GuestMapper;
import com.konstantion.dto.guest.dto.GuestDto;
import com.konstantion.guest.Guest;
import com.konstantion.guest.GuestPort;
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
import static com.konstantion.utils.EntityNameConstants.GUEST;
import static com.konstantion.utils.EntityNameConstants.GUESTS;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class GuestControllerTest {
    private static final String API_URL = "/web-api/guests";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private GuestPort guestPort;
    @Autowired
    private UserPort userPort;
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
        guestPort.deleteAll();
        userPort.deleteAll();
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
        Guest guest = Guest.builder().active(true).name("hall").build();
        guestPort.save(guest);

        EntityExchangeResult<ResponseDto<GuestDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", guest.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<GuestDto>>() {
                })
                .returnResult();

        GuestDto guestDto = result.getResponseBody()
                .data().get(GUEST);

        assertThat(guestDto).isNotNull()
                .isEqualTo(GuestMapper.INSTANCE.toDto(guest));
    }

    @Test
    void shouldReturnHallsWhenGetAllActiveHalls() {
        Guest first = Guest.builder().active(true).name("first").build();
        Guest second = Guest.builder().active(false).name("second").build();
        Guest third = Guest.builder().active(true).name("third").build();
        guestPort.save(first);
        guestPort.save(second);
        guestPort.save(third);

        EntityExchangeResult<ResponseDto<List<GuestDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<GuestDto>>>() {
                })
                .returnResult();

        List<GuestDto> guestDtos = result.getResponseBody()
                .data().get(GUESTS);

        assertThat(guestDtos)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        GuestMapper.INSTANCE.toDto(first),
                        GuestMapper.INSTANCE.toDto(third)
                );
    }
}
