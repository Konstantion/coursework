package com.konstantion.adapters.category;

import com.konstantion.ApplicationStarter;
import com.konstantion.category.Category;
import com.konstantion.configuration.RowMappersConfiguration;
import com.konstantion.testcontainers.configuration.DatabaseContainer;
import com.konstantion.testcontainers.configuration.DatabaseTestConfiguration;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {
        DatabaseTestConfiguration.class,
        RowMappersConfiguration.class,
        ApplicationStarter.class})
@Testcontainers
@ActiveProfiles("test")
class CategoryDatabaseAdapterTest {
    @ClassRule
    @Container
    public static PostgreSQLContainer<DatabaseContainer> postgresSQLContainer = DatabaseContainer.getInstance();
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    RowMappersConfiguration rowMappers;

    CategoryDatabaseAdapter categoryAdapter;

    @BeforeEach
    public void setUp() {
        categoryAdapter = new CategoryDatabaseAdapter(jdbcTemplate, rowMappers.categoryRowMapper());
    }

    @AfterEach
    void cleanUp() {
        categoryAdapter.deleteAll();
    }

    @Test
    void shouldReturnCategoryWithIdWhenSaveCategoryWithoutId() {
        Category category = Category.builder()
                .id(null)
                .name("test")
                .build();
        categoryAdapter.save(category);

        assertThat(category.getId()).isNotNull();
    }

    @Test
    void shouldUpdateCategoryWhenSaveCategoryWithId() {
        Category category = Category.builder()
                .id(null)
                .name("test")
                .build();
        categoryAdapter.save(category);
        UUID id = category.getId();

        category.setName("newName");
        categoryAdapter.save(category);

        Optional<Category> dbCategory = categoryAdapter.findById(id);

        assertThat(dbCategory).isPresent()
                .get()
                .matches(matched -> matched.getId().equals(id)
                        && matched.getName().equals("newName"));
    }

    @Test
    void shouldReturnCategoriesWhenFindAll() {
        Category first = Category.builder()
                .id(null)
                .name("first")
                .build();
        Category second = Category.builder()
                .id(null)
                .name("second")
                .build();
        categoryAdapter.save(first);
        categoryAdapter.save(second);

        List<Category> dbCategories = categoryAdapter.findAll();

        assertThat(dbCategories)
                .hasSize(2)
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void shouldDeleteCategoryWhenDeleteCategory() {
        Category first = Category.builder()
                .id(null)
                .name("first")
                .build();
        Category second = Category.builder()
                .id(null)
                .name("second")
                .build();
        categoryAdapter.save(first);
        categoryAdapter.save(second);

        categoryAdapter.delete(first);
        List<Category> dbCategories = categoryAdapter.findAll();

        assertThat(dbCategories)
                .hasSize(1)
                .containsExactlyInAnyOrder(second);
    }
}
