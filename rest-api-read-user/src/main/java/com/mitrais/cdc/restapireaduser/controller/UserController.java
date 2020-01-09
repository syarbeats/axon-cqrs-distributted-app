package com.mitrais.cdc.restapireaduser.controller;

import com.mitrais.cdc.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.List;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List<UserDto> queryResult = jdbcTemplate.query("SELECT * from user_view", (rs, rowNum) -> {
            return new UserDto(rs.getInt("id"), rs.getString("username"), rs.getString("password"), rs.getString("email"), rs.getString("firstname"), rs.getString("lastname"));
        });

        return ResponseEntity.ok(queryResult);
    }
}
