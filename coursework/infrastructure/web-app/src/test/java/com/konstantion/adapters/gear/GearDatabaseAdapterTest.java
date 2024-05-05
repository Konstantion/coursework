package com.konstantion.adapters.gear;

import com.konstantion.ApplicationStarter;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.gear.Gear;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {DatabaseTestConfiguration.class, RowMappersConfiguration.class, ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class GearDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    GearDatabaseAdapter productAdapter;

    @BeforeEach
    void setUp() {
        productAdapter = new GearDatabaseAdapter(jdbcTemplate, rowMappers.productRowMapper());
    }

    @AfterEach
    void cleanUp() {
        productAdapter.deleteAll();
    }

    @Test
    void shouldReturnProductWithIdwWhenSaveProductWithoutId() {
        Gear gear = Gear.builder()
                .name("test")
                .price(10.0)
                .active(true)
                .build();

        productAdapter.save(gear);

        assertThat(gear.getId())
                .isNotNull();
    }

    @Test
    void shouldReturnUpdatedProductWhenSaveProductWithId() {
        Gear gear = Gear.builder()
                .name("test")
                .price(10.0)
                .active(true)
                .build();
        productAdapter.save(gear);

        gear.setDescription("test product description");
        Gear dbGear = productAdapter.save(gear);

        assertThat(dbGear).isNotNull()
                .isEqualTo(gear);
    }

    @Test
    void shouldReturnProductWhenFindById() {
        Gear gear = Gear.builder()
                .name("test")
                .price(10.0)
                .active(true)
                .build();
        productAdapter.save(gear);
        UUID id = gear.getId();

        Optional<Gear> dbProduct = productAdapter.findById(id);

        assertThat(dbProduct).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(id)
                        && matched.equals(gear));
    }

    @Test
    void shouldDeleteProductWhenDelete() {
        Gear gear = Gear.builder()
                .name("test")
                .price(10.0)
                .active(true)
                .build();
        productAdapter.save(gear);
        UUID productId = gear.getId();

        productAdapter.delete(gear);
        Optional<Gear> dbProduct = productAdapter.findById(productId);

        assertThat(dbProduct).isNotPresent();
    }

    @Test
    void shouldReturnProductsWhenGetAll() {
        Gear first = Gear.builder().name("first").active(true).price(10.0).build();
        Gear second = Gear.builder().name("second").active(true).price(11.0).build();
        Gear third = Gear.builder().name("third").active(true).price(12.0).build();
        productAdapter.save(first);
        productAdapter.save(second);
        productAdapter.save(third);

        Page<Gear> page_1_size_2_orderBy_price = productAdapter.findAll(1, 2, "price", "", null, true, true);
        Page<Gear> page_1_size_4_OrderBy_price_searchPattern_i = productAdapter.findAll(1, 4, "price", "i", null, true, true);

        assertThat(page_1_size_2_orderBy_price.getContent())
                .hasSize(2)
                .containsExactly(first, second);

        assertThat(page_1_size_4_OrderBy_price_searchPattern_i)
                .hasSize(2)
                .containsExactly(first, third);
    }
}