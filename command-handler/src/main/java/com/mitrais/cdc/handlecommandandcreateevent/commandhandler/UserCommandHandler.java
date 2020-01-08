package com.mitrais.cdc.handlecommandandcreateevent.commandhandler;

import com.mitrais.cdc.command.AddUserCommand;
import com.mitrais.cdc.handlecommandandcreateevent.model.User;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCommandHandler {

    @Autowired
    @Qualifier("userRepository")
    private Repository<User> userRepository;

    @CommandHandler
    public void handle(AddUserCommand addUserCommand){
        log.info("Command Handler...");
        User user = new User(addUserCommand.getId(), addUserCommand.getUsername(), addUserCommand.getPassword(), true, addUserCommand.getEmail(), addUserCommand.getFirstname(), addUserCommand.getLastname());
        userRepository.add(user);
    }
}
