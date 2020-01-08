package com.mitrais.cdc.eventhandler.eventhandler;

import com.mitrais.cdc.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Slf4j
public class UserEventHandler {

    @Autowired
    DataSource dataSource;

    @EventHandler
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent){
        log.info("Event Handler...");
        log.info("User Created Event:{} ", userCreatedEvent.getUsername());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("INSERT INTO user_view VALUES(?,?,?,?,?,?)", new Object[]{userCreatedEvent.getId(),userCreatedEvent.getEmail(), userCreatedEvent.getPassword(), userCreatedEvent.getUsername(), userCreatedEvent.getFirstname(), userCreatedEvent.getLastname()});
    }
}
