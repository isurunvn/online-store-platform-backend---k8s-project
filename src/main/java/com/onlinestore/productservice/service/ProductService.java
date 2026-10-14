package com.onlinestore.productservice.service;

import com.onlinestore.productservice.dto.CreateProductRequest;
import com.onlinestore.productservice.dto.PagedResponse;
import com.onlinestore.productservice.dto.ProductDTO;
import com.onlinestore.productservice.dto.UpdateProductRequest;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    PagedResponse<ProductDTO> getAllProducts(Pageable pageable);
    ProductDTO getProductById(Long id);
    PagedResponse<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    PagedResponse<ProductDTO> searchProductsByName(String name, Pageable pageable);
    ProductDTO createProduct(CreateProductRequest request);
    ProductDTO updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
    ProductDTO updateStock(Long id, Integer quantity);
}
