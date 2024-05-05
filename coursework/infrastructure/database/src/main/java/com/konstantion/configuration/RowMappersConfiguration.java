package com.konstantion.configuration;

import com.konstantion.call.Call;
import com.konstantion.camp.Camp;
import com.konstantion.category.Category;
import com.konstantion.equipment.Equipment;
import com.konstantion.expedition.Expedition;
import com.konstantion.gear.Gear;
import com.konstantion.guest.Guest;
import com.konstantion.log.Log;
import com.konstantion.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;

@Configuration
public class RowMappersConfiguration {
    @Bean
    public RowMapper<Log> billRowMapper() {
        return new BeanPropertyRowMapper<>(Log.class);
    }

    @Bean
    public RowMapper<Equipment> orderRowMapper() {
        return new BeanPropertyRowMapper<>(Equipment.class);
    }

    @Bean
    public RowMapper<Call> callRowMapper() {
        return new BeanPropertyRowMapper<>(Call.class);
    }

    @Bean
    public RowMapper<Guest> guestRowMapper() {
        return new BeanPropertyRowMapper<>(Guest.class);
    }

    @Bean
    public RowMapper<Gear> productRowMapper() {
        return new BeanPropertyRowMapper<>(Gear.class);
    }

    @Bean
    public RowMapper<Category> categoryRowMapper() {
        return new BeanPropertyRowMapper<>(Category.class);
    }

    @Bean
    public RowMapper<Expedition> tableRowMapper() {
        return new BeanPropertyRowMapper<>(Expedition.class);
    }

    @Bean
    public RowMapper<User> userRowMapper() {
        return new BeanPropertyRowMapper<>(User.class);
    }

    @Bean
    public RowMapper<Camp> hallRowMapper() {
        return new BeanPropertyRowMapper<>(Camp.class);
    }
}
