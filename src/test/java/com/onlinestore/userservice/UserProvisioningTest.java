package com.onlinestore.userservice;
import com.onlinestore.userservice.repository.*;
import com.onlinestore.userservice.entity.User;
import com.onlinestore.userservice.service.UserServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class UserProvisioningTest {
    @Test void missingEmailsDoNotShareTheSameUniquePlaceholder() {
        var users = mock(UserRepository.class);
        when(users.findByKeycloakUserId(anyString())).thenReturn(Optional.empty());
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        var service = new UserServiceImpl(users, mock(AddressRepository.class));
        var first = service.getCurrentUser("customer-a", null, null, null);
        var second = service.getCurrentUser("customer-b", null, null, null);
        assertNotEquals(first.getEmail(), second.getEmail());
    }
}
