package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.Product;

import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(String id);
}
