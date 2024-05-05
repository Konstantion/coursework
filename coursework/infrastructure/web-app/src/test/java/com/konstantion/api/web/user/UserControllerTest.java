package com.konstantion.api.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.konstantion.dto.user.converter.UserMapper;
import com.konstantion.dto.user.dto.UserDto;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.Set;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.USER;
import static com.konstantion.utils.EntityNameConstants.USERS;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {
    private static final UserMapper userMapper = UserMapper.INSTANCE;
    private static final String API_URL = "/web-api/users";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    ObjectMapper objectMapper;
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserPort userPort;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        userPort.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        userPort.deleteAll();
    }

    @Test
    void shouldReturnForbiddenWhenGetUserByIdUnauthorized() {
        webTestClient.get()
                .uri(format("%s/%s", API_URL, UUID.randomUUID()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnUserWhenGetUserByIdWithRoleUser() {
        User dbUser = User.builder()
                .email("email")
                .password(passwordEncoder.encode("password"))
                .active(true)
                .build();
        userPort.save(dbUser);
        User waiter = User.builder()
                .active(true)
                .email("user")
                .password("user")
                .permissions(Permission.getDefaultWaiterPermission())
                .roles(Role.getWaiterRole())
                .build();
        userPort.save(waiter);

        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        String jwtToken = jwtService.generateToken(extraClaim, waiter);

        EntityExchangeResult<ResponseDto<UserDto>> result = webTestClient.get()
                .uri(format("%s/%s", API_URL, dbUser.getId()))
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<UserDto>>() {
                }).returnResult();

        UserDto userDto = result.getResponseBody()
                .data().get(USER);

        assertThat(userDto)
                .isEqualTo(userMapper.toDto(dbUser));
    }

    @Test
    void shouldReturnAllUserWhenGetAllActiveUsers() {
        User dbUser = User.builder()
                .email("email")
                .password(passwordEncoder.encode("password"))
                .active(true)
                .build();
        userPort.save(dbUser);
        User waiter = User.builder()
                .active(true)
                .email("user")
                .password("user")
                .permissions(Permission.getDefaultWaiterPermission())
                .roles(Role.getWaiterRole())
                .build();
        userPort.save(waiter);
        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        String jwtToken = jwtService.generateToken(extraClaim, waiter);

        EntityExchangeResult<ResponseDto<List<UserDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<UserDto>>>() {
                }).returnResult();
        List<UserDto> userDtos = result.getResponseBody()
                .data().get(USERS);

        assertThat(userDtos)
                .hasSize(2)
                .containsExactlyInAnyOrder(userMapper.toDto(waiter), userMapper.toDto(dbUser));
    }

    @Test
    void shouldReturnUserWhenGetUserByIdWithRoleAdmin() {
        User dbUser = User.builder()
                .email("email")
                .password(passwordEncoder.encode("password"))
                .active(true)
                .build();
        userPort.save(dbUser);
        User admin = User.builder()
                .active(true)
                .email("user")
                .password("user")
                .permissions(Set.of(Permission.SUPER_USER))
                .roles(Role.getAdminRole())
                .build();
        userPort.save(admin);

        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        String jwtToken = jwtService.generateToken(extraClaim, admin);

        EntityExchangeResult<ResponseDto<UserDto>> result = webTestClient.get()
                .uri(format("%s/%s", API_URL, dbUser.getId()))
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<UserDto>>() {
                }).returnResult();

        UserDto userDto = result.getResponseBody()
                .data().get(USER);

        assertThat(userDto)
                .isEqualTo(userMapper.toDto(dbUser));
    }

    @Test
    void shouldReturnBadRequestWhenGetUserByIdWithNonExistingId() {
        User dbUser = User.builder()
                .email("email")
                .password(passwordEncoder.encode("password"))
                .active(true)
                .build();
        userPort.save(dbUser);
        User waiter = User.builder()
                .active(true)
                .email("user")
                .password("user")
                .permissions(Permission.getDefaultWaiterPermission())
                .roles(Role.getWaiterRole())
                .build();
        userPort.save(waiter);

        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        String jwtToken = jwtService.generateToken(extraClaim, waiter);

        webTestClient.get()
                .uri(format("%s/%s", API_URL, UUID.randomUUID()))
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
