package com.onlinestore.orderservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinestore.orderservice.dto.CreateOrderRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OrderContractTest {
    private final ObjectMapper mapper = new ObjectMapper();
    @Test void checkoutItemsDeserializeAndValidate() throws Exception {
        var request = mapper.readValue("{\"items\":[{\"productId\":1,\"quantity\":2}]}", CreateOrderRequest.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertTrue(factory.getValidator().validate(request).isEmpty());
        }
        assertEquals(2, request.getItems().get(0).getQuantity());
    }
    @Test void negativeQuantityIsRejected() throws Exception {
        var request = mapper.readValue("{\"items\":[{\"productId\":1,\"quantity\":-1}]}", CreateOrderRequest.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            assertFalse(factory.getValidator().validate(request).isEmpty());
        }
    }
}
