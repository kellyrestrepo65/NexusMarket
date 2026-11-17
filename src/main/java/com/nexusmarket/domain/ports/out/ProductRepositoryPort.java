package com.nexusmarket.domain.ports.out;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(EntityId id);

    List<Product> findBySellerId(EntityId sellerId);

    List<Product> findByStatus(ProductStatus status);
}
