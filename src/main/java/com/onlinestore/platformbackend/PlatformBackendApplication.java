package com.onlinestore.platformbackend;

import com.onlinestore.orderservice.entity.Order;
import com.onlinestore.orderservice.repository.OrderRepository;
import com.onlinestore.productservice.entity.Category;
import com.onlinestore.productservice.entity.Product;
import com.onlinestore.productservice.repository.CategoryRepository;
import com.onlinestore.productservice.repository.ProductRepository;
import com.onlinestore.userservice.entity.Address;
import com.onlinestore.userservice.entity.User;
import com.onlinestore.userservice.repository.AddressRepository;
import com.onlinestore.userservice.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.onlinestore")
@EntityScan(basePackageClasses = {
        Order.class,
        Product.class,
        Category.class,
        User.class,
        Address.class
})
@EnableJpaRepositories(basePackageClasses = {
        OrderRepository.class,
        ProductRepository.class,
        CategoryRepository.class,
        UserRepository.class,
        AddressRepository.class
})
public class PlatformBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformBackendApplication.class, args);
    }
}