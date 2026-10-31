package com.onlinestore.userservice.service;

import com.onlinestore.userservice.dto.AddressDTO;
import com.onlinestore.userservice.dto.CreateAddressRequest;
import com.onlinestore.userservice.dto.UpdateUserRequest;
import com.onlinestore.userservice.dto.UserDTO;
import com.onlinestore.userservice.entity.Address;
import com.onlinestore.userservice.entity.User;
import com.onlinestore.userservice.exception.ResourceNotFoundException;
import com.onlinestore.userservice.repository.AddressRepository;
import com.onlinestore.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional
    public UserDTO getCurrentUser(String keycloakUserId, String email, String firstName, String lastName) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .keycloakUserId(keycloakUserId)
                            .email(email != null ? email : keycloakUserId + "@users.invalid")
                            .firstName(firstName != null ? firstName : "Unknown")
                            .lastName(lastName != null ? lastName : "Unknown")
                            .build();
                    return userRepository.save(newUser);
                });
        // Fill claims when an older order-created profile used placeholder values.
        if (email != null && !email.isBlank() && (user.getEmail().endsWith("@users.invalid") || user.getEmail().equals("unknown@example.com"))) {
            user.setEmail(email);
        }
        if (firstName != null && "Unknown".equals(user.getFirstName())) user.setFirstName(firstName);
        if (lastName != null && "Unknown".equals(user.getLastName())) user.setLastName(lastName);
        return mapToUserDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByKeycloakId(String keycloakUserId) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + keycloakUserId));
        return mapToUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateCurrentUser(String keycloakUserId, UpdateUserRequest request) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + keycloakUserId));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user = userRepository.save(user);
        return mapToUserDTO(user);
    }

    @Override
    @Transactional
    public AddressDTO addAddress(String keycloakUserId, CreateAddressRequest request) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + keycloakUserId));

        Address address = Address.builder()
                .user(user)
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build();

        address = addressRepository.save(address);
        return mapToAddressDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDTO> getAddresses(String keycloakUserId) {
        User user = userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + keycloakUserId));

        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream().map(this::mapToAddressDTO).collect(Collectors.toList());
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .keycloakUserId(user.getKeycloakUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private AddressDTO mapToAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }
}
