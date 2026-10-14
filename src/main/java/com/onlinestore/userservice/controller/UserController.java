package com.onlinestore.userservice.controller;

import com.onlinestore.userservice.dto.AddressDTO;
import com.onlinestore.userservice.dto.CreateAddressRequest;
import com.onlinestore.userservice.dto.UpdateUserRequest;
import com.onlinestore.userservice.dto.UserDTO;
import com.onlinestore.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakUserId = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");
        
        return ResponseEntity.ok(userService.getCurrentUser(keycloakUserId, email, firstName, lastName));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateCurrentUser(Authentication authentication, 
                                                     @Valid @RequestBody UpdateUserRequest request) {
        String keycloakUserId = authentication.getName();
        return ResponseEntity.ok(userService.updateCurrentUser(keycloakUserId, request));
    }

    @PostMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> addAddress(Authentication authentication, 
                                                 @Valid @RequestBody CreateAddressRequest request) {
        String keycloakUserId = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(keycloakUserId, request));
    }

    @GetMapping("/address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressDTO>> getAddresses(Authentication authentication) {
        String keycloakUserId = authentication.getName();
        return ResponseEntity.ok(userService.getAddresses(keycloakUserId));
    }

    @GetMapping("/{keycloakUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserByKeycloakId(@PathVariable String keycloakUserId) {
        return ResponseEntity.ok(userService.getUserByKeycloakId(keycloakUserId));
    }
}
