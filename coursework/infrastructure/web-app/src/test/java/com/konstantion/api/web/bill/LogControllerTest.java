package com.konstantion.api.web.bill;

import com.github.javafaker.Faker;
import com.google.common.collect.Lists;
import com.konstantion.dto.bill.converter.BillMapper;
import com.konstantion.dto.bill.dto.BillDto;
import com.konstantion.dto.bill.dto.CreateBillRequestDto;
import com.konstantion.equipment.Equipment;
import com.konstantion.equipment.EquipmentPort;
import com.konstantion.gear.Gear;
import com.konstantion.gear.GearPort;
import com.konstantion.guest.Guest;
import com.konstantion.guest.GuestPort;
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
import java.util.UUID;

import static com.konstantion.utils.DoubleUtils.percent;
import static com.konstantion.utils.DoubleUtils.round;
import static com.konstantion.utils.EntityNameConstants.BILL;
import static com.konstantion.utils.EntityNameConstants.BILLS;
import static com.konstantion.utils.EntityNameConstants.ENTITY;
import static com.konstantion.utils.EntityNameConstants.USER;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
@ContextConfiguration(classes = {DatabaseTestConfiguration.class})
@ActiveProfiles("test")
class LogControllerTest {
    private static final String API_URL = "/web-api/bills";
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    Faker faker;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private LogPort logPort;
    @Autowired
    private UserPort userPort;
    @Autowired
    private GuestPort guestPort;
    @Autowired
    private EquipmentPort equipmentPort;
    @Autowired
    private GearPort gearPort;
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
        logPort.deleteAll();
        userPort.deleteAll();
        equipmentPort.deleteAll();
        guestPort.deleteAll();
        gearPort.deleteAll();
    }

    @Test
    void shouldReturnAllActiveBillsWhenGetAllActiveBills() {
        for (int i = 0; i < 3; i++) {
            Equipment equipment = Equipment.builder().active(true).build();
            equipmentPort.save(equipment);
            Log log = Log.builder().equipmentId(equipment.getId()).price(10.0).priceWithDiscount(10.0).active(true).build();
            if ((i & 1) == 1) log.setActive(false);
            logPort.save(log);
        }

        EntityExchangeResult<ResponseDto<List<BillDto>>> result = webTestClient.get()
                .uri(API_URL)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<List<BillDto>>>() {
                })
                .returnResult();

        List<BillDto> billDtos = result.getResponseBody()
                .data().get(BILLS);

        assertThat(billDtos)
                .hasSize(2)
                .allMatch(BillDto::active);
    }

    @Test
    void shouldReturnBillWhenGetBillById() {
        Equipment equipment = Equipment.builder().active(true).build();
        equipmentPort.save(equipment);
        Log log = Log.builder().equipmentId(equipment.getId()).price(10.0).priceWithDiscount(10.0).active(true).build();
        logPort.save(log);

        EntityExchangeResult<ResponseDto<BillDto>> result = webTestClient.get()
                .uri(API_URL + "/{id}", log.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<BillDto>>() {
                })
                .returnResult();

        BillDto billDto = result.getResponseBody()
                .data().get(BILL);

        assertThat(billDto).isNotNull()
                .isEqualTo(BillMapper.INSTANCE.toDto(log));
    }

    @Test
    void shouldReturnBadRequestWhenGetBillByIdWithNonExistingId() {
        webTestClient.get()
                .uri(API_URL + "/{id}", UUID.randomUUID())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldCreateBillWhenCreateBill() {
        Guest guest = Guest.builder().active(true).name("guest").discountPercent(10.0).build();
        guestPort.save(guest);

        Gear gear = Gear.builder().name("product").price(11.00).active(true).build();
        gearPort.save(gear);

        Equipment equipment = Equipment.builder().active(true).gearsId(Lists.newArrayList(gear.getId(), gear.getId())).build();
        equipmentPort.save(equipment);

        CreateBillRequestDto requestDto = new CreateBillRequestDto(equipment.getId(), guest.getId());

        EntityExchangeResult<ResponseDto<BillDto>> result = webTestClient.post()
                .uri(API_URL)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .bodyValue(requestDto)
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<BillDto>>() {
                })
                .returnResult();

        BillDto billDto = result.getResponseBody()
                .data().get(BILL);

        assertThat(billDto).isNotNull()
                .matches(bill ->
                        bill.price().equals(22.0)
                                && bill.priceWithDiscount()
                                .equals(round(22.0 - percent(22.0, guest.getDiscountPercent()), 2)));


    }

    @Test
    void shouldCloseBillWhenCloseBill() {
        Equipment equipment = Equipment.builder().active(true).build();
        equipmentPort.save(equipment);

        Log log = Log.builder().active(true).equipmentId(equipment.getId()).price(10.0).priceWithDiscount(10.0).build();
        logPort.save(log);

        EntityExchangeResult<ResponseDto<BillDto>> result = webTestClient.put()
                .uri(API_URL + "/{id}/close", log.getId())
                .header(HttpHeaders.AUTHORIZATION, format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<ResponseDto<BillDto>>() {
                })
                .returnResult();

        BillDto billDto = result.getResponseBody()
                .data().get(BILL);

        assertThat(billDto).isNotNull()
                .extracting(BillDto::active)
                .isEqualTo(false);
    }
}