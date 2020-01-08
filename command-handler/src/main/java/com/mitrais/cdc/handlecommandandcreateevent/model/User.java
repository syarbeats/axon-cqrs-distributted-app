package com.mitrais.cdc.handlecommandandcreateevent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mitrais.cdc.event.UserCreatedEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.axonframework.domain.AbstractAggregateRoot;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(exclude = { "id" })
public class User extends AbstractAggregateRoot<Integer> {

    @Id
    private Integer id;

    private String username;
    private String password;
    private boolean enabled;
    private String email;
    private String firstname;
    private String lastname;

    public User(Integer id, String username, String password, boolean enabled, String email, String firstname, String lastname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        registerEvent(new UserCreatedEvent(id, email, password, username, firstname, lastname ));
    }

    @Override
    public Integer getIdentifier() {
        return id;
    }
}
