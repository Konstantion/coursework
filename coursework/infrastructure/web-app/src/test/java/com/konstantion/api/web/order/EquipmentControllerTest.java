package com.konstantion.api.web.order;

import com.github.javafaker.Faker;
import com.konstantion.dto.order.converter.OrderMapper;
import com.konstantion.dto.order.dto.OrderDto;
import com.konstantion.dto.order.dto.OrderProductsRequestDto;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.expedition.Expedition;
import com.konstantion.expedition.ExpeditionPort;
import com.konstantion.expedition.ExpeditionType;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.jwt.JwtService;
import com.konstantion.log.Log;
import com.konstantion.log.LogPort;
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
import java.util.UUID;

import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.ORDER;
import static com.konstantion.utils.EntityNameConstants.ORDERS;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class EquipmentControllerTest {
    private static final String API_URL = "/web-api/orders";
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
    private GearPort gearPort;
    @Autowired
    private EquipmentPort equipmentPort;
    @Autowired
    private LogPort logPort;
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
        waiter.getPermissions().add(Permission.DELETE_GEAR_FROM_EQUIPMENT);
        userPort.save(waiter);
        Map<String, Object> extraClaim = Map.of(ENTITY, USER);
        jwtToken = jwtService.generateToken(extraClaim, waiter);
    }

    @AfterEach
    void cleanUp() {
        expeditionPort.deleteAll();
        userPort.deleteAll();
        equipmentPort.deleteAll();
        logPort.deleteAll();
        gearPort.deleteAll();
    }

    @Test
    void shouldReturnForbiddenWhenUnauthorizedUser() {
        webTestClient.get()
                .uri(API_URL)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturnActiveOrderWhenGetAllActiveOrder() {
        Equipment first = Equipment.builder().active(true).build();
        Equipment second = Equipment.builder().active(true).build();
        Equipment third = Equipment.builder().active(false).build();
        equipmentPort.save(first);
        equipmentPort.save(second);
        equipmentPort.save(third);

        EntityExchangeResult<ResponseDto<List<OrderDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<OrderDto>>>() {
                })
                .returnResult();

        List<OrderDto> orderDtos = result.getResponseBody()
                .data().get(ORDERS);

        assertThat(orderDtos)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        OrderMapper.INSTANCE.toDto(first),
                        OrderMapper.INSTANCE.toDto(second)
                );
    }

    @Test
    void shouldReturnBadRequestWhenGetOrderByIdWithNonExistingId() {
        Equipment equipment = Equipment.builder().active(true).build();
        equipmentPort.save(equipment);

        webTestClient.get()
                .uri(API_URL + "/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnOrderGetOrderById() {
        Equipment equipment = Equipment.builder().active(true).build();
        equipmentPort.save(equipment);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", equipment.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                })
                .returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);

        assertThat(orderDto).isNotNull()
                .isEqualTo(OrderMapper.INSTANCE.toDto(equipment));
    }

    @Test
    void shouldDeleteOrderWhenCloseOrderIfOrderWithoutProducts() {
        Equipment equipment = Equipment.builder().active(true).build();
        equipmentPort.save(equipment);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.put()
                .uri(API_URL + "/{id}/close", equipment.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                })
                .returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);
        Optional<Equipment> dbOrder = equipmentPort.findById(equipment.getId());

        assertThat(orderDto).isNotNull()
                .isEqualTo(OrderMapper.INSTANCE.toDto(equipment));
        assertThat(dbOrder).isNotPresent();
    }

    @Test
    void shouldCloseOrderWhenCloseOrder() {
        Gear gear = Gear.builder()
                .active(true)
                .name("product")
                .price(10.0)
                .build();
        gearPort.save(gear);

        Expedition table = Expedition.builder()
                .active(true)
                .name("test")
                .password("test")
                .expeditionType(ExpeditionType.COMMON)
                .build();
        expeditionPort.save(table);

        Equipment equipment = Equipment.builder()
                .active(true)
                .expeditionId(table.getId())
                .gearsId(List.of(gear.getId()))
                .build();
        equipmentPort.save(equipment);
        table.setEquipmentId(equipment.getId());
        expeditionPort.save(table);

        Log log = Log.builder()
                .active(false)
                .equipmentId(equipment.getId())
                .price(10.0)
                .priceWithDiscount(10.0)
                .build();
        logPort.save(log);
        equipment.setLogId(log.getId());
        equipmentPort.save(equipment);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.put()
                .uri(API_URL + "/{id}/close", equipment.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                })
                .returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);
        Optional<Equipment> dbOrder = equipmentPort.findById(equipment.getId());

        assertThat(dbOrder).isPresent()
                .get().extracting(Equipment::isActive).isEqualTo(false);
    }

    @Test
    void shouldAddProductToOrderWhenAddProductToOrder() {
        Gear gear = Gear.builder()
                .active(true)
                .name("product")
                .price(10.0)
                .build();
        gearPort.save(gear);

        Equipment equipment = Equipment.builder()
                .active(true)
                .build();
        equipmentPort.save(equipment);

        OrderProductsRequestDto requestDto = new OrderProductsRequestDto(gear.getId(), 5);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.put()
                .uri(API_URL + "/{id}/products", equipment.getId())
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(requestDto)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                })
                .returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);

        assertThat(orderDto.productsId())
                .hasSize(5)
                .containsOnly(gear.getId());
    }

    @Test
    void shouldRemoveProductFromOrderWhenRemoveProductToOrder() {
        Gear gear = Gear.builder()
                .active(true)
                .name("product")
                .price(10.0)
                .build();
        gearPort.save(gear);

        Equipment equipment = Equipment.builder()
                .active(true)
                .gearsId(List.of(gear.getId(), gear.getId(), gear.getId()))
                .build();
        equipmentPort.save(equipment);

        OrderProductsRequestDto requestDto = new OrderProductsRequestDto(gear.getId(), 2);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.put()
                .uri(API_URL + "/{id}/products/remove", equipment.getId())
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(requestDto)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                })
                .returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);

        assertThat(orderDto.productsId())
                .hasSize(1)
                .containsOnly(gear.getId());
    }

    @Test
    void shouldTransferOrderWhenTransferOrder() {
        Expedition first = Expedition.builder().active(true).name("first").password("first").build();
        Expedition second = Expedition.builder().active(true).name("second").password("second").build();
        expeditionPort.save(first);
        expeditionPort.save(second);

        Equipment equipment = Equipment.builder().active(true).expeditionId(first.getId()).build();
        equipmentPort.save(equipment);

        first.setEquipmentId(equipment.getId());
        expeditionPort.save(first);

        EntityExchangeResult<ResponseDto<OrderDto>> result = webTestClient.put()
                .uri(API_URL + "/{orderId}/transfer/tables/{tableId}", equipment.getId(), second.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<OrderDto>>() {
                }).returnResult();

        OrderDto orderDto = result.getResponseBody()
                .data().get(ORDER);

        assertThat(orderDto).isNotNull()
                .extracting(OrderDto::tableId)
                .isEqualTo(second.getId());
    }
}