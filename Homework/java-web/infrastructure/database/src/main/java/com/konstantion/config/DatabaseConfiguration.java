package com.konstantion.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DatabaseConfiguration {
    @Bean
    @Qualifier("postgresDB")
    public DataSource hikariDataSource(
            @Value("${application.database.url}") String url,
            @Value("${application.database.username}") String username,
            @Value("${application.database.password}") String password,
            @Value("${application.database.driver-class-name}") String driverClassName,
            @Value("${application.database.max-pool-size}") Integer maxPoolSize
    ) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(maxPoolSize);
        hikariConfig.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public NamedParameterJdbcTemplate jdbcTemplate(
            @Qualifier("postgresDB") DataSource hikariDataSource
    ) {
        return new NamedParameterJdbcTemplate(hikariDataSource);
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            @Qualifier("postgresDB") DataSource dataSource,
            @Qualifier("flywayProps") Properties properties
    ) {
        return Flyway.configure()
                .dataSource(dataSource)
                .outOfOrder(true)
                .locations("classpath:/db/migration")
                .cleanDisabled(true)
                .baselineOnMigrate(true)
                .configuration(properties)
                .load();
    }
}
