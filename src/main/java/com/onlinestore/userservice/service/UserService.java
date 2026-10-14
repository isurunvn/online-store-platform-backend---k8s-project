package com.onlinestore.userservice.service;

import com.onlinestore.userservice.dto.AddressDTO;
import com.onlinestore.userservice.dto.CreateAddressRequest;
import com.onlinestore.userservice.dto.UpdateUserRequest;
import com.onlinestore.userservice.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO getCurrentUser(String keycloakUserId, String email, String firstName, String lastName);
    UserDTO getUserByKeycloakId(String keycloakUserId);
    UserDTO updateCurrentUser(String keycloakUserId, UpdateUserRequest request);
    AddressDTO addAddress(String keycloakUserId, CreateAddressRequest request);
    List<AddressDTO> getAddresses(String keycloakUserId);
}
