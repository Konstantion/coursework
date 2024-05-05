package com.konstantion.api.web.category;

import com.github.javafaker.Faker;
import com.konstantion.category.Category;
import com.konstantion.category.CategoryPort;
import com.konstantion.dto.category.converter.CategoryMapper;
import com.konstantion.dto.category.dto.CategoryDto;
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

import static com.konstantion.utils.EntityNameConstants.CATEGORIES;
import static com.konstantion.utils.EntityNameConstants.CATEGORY;
import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class CategoryControllerTest {
    private static final String API_URL = "/web-api/categories";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private CategoryPort categoryPort;
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
        categoryPort.deleteAll();
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
        Category category = Category.builder().name("category").build();
        categoryPort.save(category);

        EntityExchangeResult<ResponseDto<CategoryDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", category.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<CategoryDto>>() {
                })
                .returnResult();

        CategoryDto categoryDto = result.getResponseBody()
                .data().get(CATEGORY);

        assertThat(categoryDto).isNotNull()
                .isEqualTo(CategoryMapper.INSTANCE.toDto(category));
    }

    @Test
    void shouldReturnHallsWhenGetAllActiveHalls() {
        Category first = Category.builder().name("first").build();
        Category second = Category.builder().name("second").build();
        Category third = Category.builder().name("third").build();
        categoryPort.save(first);
        categoryPort.save(second);
        categoryPort.save(third);

        EntityExchangeResult<ResponseDto<List<CategoryDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<CategoryDto>>>() {
                })
                .returnResult();

        List<CategoryDto> categoryDtos = result.getResponseBody()
                .data().get(CATEGORIES);

        assertThat(categoryDtos)
                .hasSize(3)
                .containsExactlyInAnyOrder(
                        CategoryMapper.INSTANCE.toDto(first),
                        CategoryMapper.INSTANCE.toDto(second),
                        CategoryMapper.INSTANCE.toDto(third)
                );
    }
}
