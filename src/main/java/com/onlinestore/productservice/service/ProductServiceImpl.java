package com.onlinestore.productservice.service;

import com.onlinestore.productservice.dto.CreateProductRequest;
import com.onlinestore.productservice.dto.PagedResponse;
import com.onlinestore.productservice.dto.ProductDTO;
import com.onlinestore.productservice.dto.UpdateProductRequest;
import com.onlinestore.productservice.entity.Category;
import com.onlinestore.productservice.entity.Product;
import com.onlinestore.productservice.exception.ResourceNotFoundException;
import com.onlinestore.productservice.repository.CategoryRepository;
import com.onlinestore.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = "products", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public PagedResponse<ProductDTO> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return createPagedResponse(productsPage);
    }

    @Override
    @Cacheable(value = "product-by-id", key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Override
    public PagedResponse<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        Page<Product> productsPage = productRepository.findByCategoryId(categoryId, pageable);
        return createPagedResponse(productsPage);
    }

    @Override
    public PagedResponse<ProductDTO> searchProductsByName(String name, Pageable pageable) {
        Page<Product> productsPage = productRepository.findByNameContainingIgnoreCase(name, pageable);
        return createPagedResponse(productsPage);
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public ProductDTO createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Override
    @CacheEvict(value = {"products", "product-by-id"}, allEntries = true)
    @org.springframework.transaction.annotation.Transactional
    public ProductDTO updateStock(Long id, Integer quantity) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (product.getStockQuantity() + quantity < 0) {
            throw new com.onlinestore.orderservice.exception.InsufficientStockException("Insufficient stock for product id: " + id);
        }
        product.setStockQuantity(product.getStockQuantity() + quantity);
        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .build();
    }

    private PagedResponse<ProductDTO> createPagedResponse(Page<Product> page) {
        List<ProductDTO> content = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PagedResponse.<ProductDTO>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
