package com.konstantion.api.web.product;

import com.github.javafaker.Faker;
import com.konstantion.api.util.RestResponsePage;
import com.konstantion.category.Category;
import com.konstantion.category.CategoryPort;
import com.konstantion.dto.product.converter.ProductMapper;
import com.konstantion.dto.product.dto.ProductDto;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.PRODUCT;
import static com.konstantion.utils.EntityNameConstants.PRODUCTS;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class GearControllerTest {
    private static final String API_URL = "/web-api/products";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
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
    private CategoryPort categoryPort;
    @Autowired
    private GearPort gearPort;
    private User waiter;
    private String jwtToken;

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
        userPort.deleteAll();
        categoryPort.deleteAll();
        gearPort.deleteAll();
    }

    @Test
    void shouldThrowForbiddenWhenUnauthorizedUser() {
        webTestClient.get()
                .uri(API_URL)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnProductsWhenGetAllActiveProducts() {
        Gear first = Gear.builder().name(faker.name().name()).price(faker.random().nextDouble()).active(true).build();
        Gear second = Gear.builder().name(faker.name().name()).price(faker.random().nextDouble()).active(true).build();
        gearPort.save(first);
        gearPort.save(second);

        EntityExchangeResult<ResponseDto<RestResponsePage<ProductDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<RestResponsePage<ProductDto>>>() {
                }).returnResult();


        Page<ProductDto> productDtos = result.getResponseBody()
                .data().get(PRODUCTS);

        assertThat(productDtos)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        ProductMapper.INSTANCE.toDto(first),
                        ProductMapper.INSTANCE.toDto(second)
                );
    }

    @Test
    void shouldReturnProductsWithCategoryIdWhenGetAllActiveProductsWithParameters() {
        Category category = Category.builder().name(faker.name().name()).build();
        categoryPort.save(category);
        Gear first = Gear.builder().categoryId(category.getId()).name(faker.name().name()).price(faker.random().nextDouble()).active(true).build();
        Gear second = Gear.builder().name(faker.name().name()).price(faker.random().nextDouble()).active(true).build();
        gearPort.save(first);
        gearPort.save(second);

        EntityExchangeResult<ResponseDto<RestResponsePage<ProductDto>>> result = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(API_URL)
                        .queryParam("category", category.getId())
                        .build())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<RestResponsePage<ProductDto>>>() {
                }).returnResult();

        Page<ProductDto> productDtos = result.getResponseBody()
                .data().get(PRODUCTS);

        assertThat(productDtos)
                .hasSize(1)
                .containsExactlyInAnyOrder(
                        ProductMapper.INSTANCE.toDto(first)
                );
    }

    @Test
    void shouldReturnProductWhenGetProductById() {
        Gear gear = Gear.builder().name(faker.name().name()).price(faker.random().nextDouble()).active(true).build();
        gearPort.save(gear);

        EntityExchangeResult<ResponseDto<ProductDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", gear.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<ProductDto>>() {
                }).returnResult();

        ProductDto productDto = result.getResponseBody()
                .data().get(PRODUCT);

        assertThat(productDto).isNotNull()
                .isEqualTo(ProductMapper.INSTANCE.toDto(gear));
    }

    @Test
    void shouldBadRequestWhenGetProductById() {
        webTestClient.get()
                .uri(API_URL + "/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
