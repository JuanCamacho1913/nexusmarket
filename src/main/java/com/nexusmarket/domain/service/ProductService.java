package com.nexusmarket.domain.service;

import com.nexusmarket.domain.models.Product;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.exception.EntityNotFoundException;
import com.nexusmarket.domain.exception.InvariantViolationException;
import com.nexusmarket.domain.exception.ValidationException;
import com.nexusmarket.domain.service.support.IdGenerator;
import com.nexusmarket.domain.valueObjects.ProductStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepositoryPort productRepository;
    private final IdGenerator idGenerator;

    public Product createProduct(Product product) {
        if (product.getSellerProfile() == null) {
            throw new ValidationException("Product.sellerProfile must not be null");
        }
        product.setId(idGenerator.newId());
        return productRepository.save(product);
    }

    public Product publish(String productId) {
        Product product = requireTransitionable(productId);
        product.setStatus(ProductStatus.PUBLISHED);
        return productRepository.save(product);
    }

    public Product suspend(String productId) {
        Product product = requireTransitionable(productId);
        product.setStatus(ProductStatus.SUSPENDED);
        return productRepository.save(product);
    }

    public Product discontinue(String productId) {
        Product product = requireTransitionable(productId);
        product.setStatus(ProductStatus.DISCONTINUED);
        return productRepository.save(product);
    }

    private Product requireTransitionable(String productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product '" + productId + "' does not exist"));
        if (product.getStatus() == ProductStatus.DISCONTINUED) {
            throw new InvariantViolationException(
                    "Product '" + productId + "' is DISCONTINUED and cannot transition further");
        }
        return product;
    }
}
