package com.mitrais.cdc.restapi.controller;

import com.mitrais.cdc.command.AddUserCommand;
import com.mitrais.cdc.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    @Qualifier("distributedCommandGateway")
    private CommandGateway commandGateway;

    @PostMapping("/add-user")
    @Transactional
    public ResponseEntity<UserDto> addUser(@RequestBody UserDto user){
        log.info("Add-User....");
        log.info("User Data-ID: {}", user.getId());
        log.info("User Data-Name: {}", user.getFirstname());
        commandGateway.sendAndWait(new AddUserCommand(user.getId(), user.getEmail(), user.getPassword(), user.getUsername(), user.getFirstname(), user.getLastname()));
        return ResponseEntity.ok(user);
    }
}
