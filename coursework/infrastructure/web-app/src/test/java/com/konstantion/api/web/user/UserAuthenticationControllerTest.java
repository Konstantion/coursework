package com.konstantion.api.web.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.konstantion.dto.authentication.dto.AuthenticationResponseDto;
import com.konstantion.dto.user.dto.LoginUserRequestDto;
import com.konstantion.dto.user.dto.UserDto;
import com.konstantion.jwt.JwtService;
import com.konstantion.response.ResponseDto;
import com.konstantion.security.jwt.config.JwtConfig;
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

import java.util.Set;

import static com.konstantion.utils.EntityNameConstants.AUTHENTICATION;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class UserAuthenticationControllerTest {
    private static final String API_URL = "/web-api/authentication";
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
    @Autowired
    private JwtConfig jwtConfig;

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
    void shouldReturnUnauthorizedWhenInvalidPassword() {
        LoginUserRequestDto requestDto = new LoginUserRequestDto("invalid");

        webTestClient.post()
                .uri(API_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnBarerTokenWhenValidPassword() throws JsonProcessingException {
        String password = "admin";
        User user = User.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .password(passwordEncoder.encode(password))
                .createdAt(now())
                .email("admin")
                .active(true)
                .roles(Role.getAdminRole())
                .permissions(Set.of(Permission.SUPER_USER))
                .build();
        userPort.save(user);
        LoginUserRequestDto requestDto = new LoginUserRequestDto(password);

        EntityExchangeResult<ResponseDto<AuthenticationResponseDto<UserDto>>> result = webTestClient.post()
                .uri(API_URL)
                .contentType(APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<AuthenticationResponseDto<UserDto>>>() {
                })
                .returnResult();

        String jwtToken = result.getResponseHeaders()
                .get(HttpHeaders.AUTHORIZATION)
                .get(0).substring(jwtConfig.getTokenPrefix().length());
        AuthenticationResponseDto<UserDto> authenticationResponse = result.getResponseBody()
                .data().get(AUTHENTICATION);

        assertThat(jwtService.isTokenValid(jwtToken, user)).isTrue();
        assertThat(authenticationResponse.token()).isEqualTo(jwtToken);
        assertThat(authenticationResponse.authenticated().get(USER).id()).isEqualTo(user.getId());
    }
}
