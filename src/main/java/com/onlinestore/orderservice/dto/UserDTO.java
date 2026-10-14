package com.onlinestore.orderservice.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String keycloakUserId;
    private String firstName;
    private String lastName;
    private String email;
}
