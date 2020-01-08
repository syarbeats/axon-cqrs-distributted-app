package com.mitrais.cdc.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String firstname;
    private String lastname;

    public UserDto(Integer id, String username, String password, String email, String firstname, String lastname) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public UserDto() {
        super();
    }
}
