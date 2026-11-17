package com.nexusmarket.application.adapters.out.persistence.mongodb.adapters;

import com.nexusmarket.application.adapters.out.persistence.mongodb.documents.ProductDocument;
import com.nexusmarket.application.adapters.out.persistence.mongodb.repositories.ProductMongoRepository;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductMongoAdapter implements ProductRepositoryPort {

    private final ProductMongoRepository productMongoRepository;

    public ProductMongoAdapter(ProductMongoRepository productMongoRepository) {
        this.productMongoRepository = productMongoRepository;
    }

    @Override
    public Product save(Product product) {
        productMongoRepository.save(ProductDocument.from(product));
        return product;
    }

    @Override
    public Optional<Product> findById(EntityId id) {
        return productMongoRepository.findById(id.value()).map(ProductDocument::toDomain);
    }

    @Override
    public List<Product> findBySellerId(EntityId sellerId) {
        return productMongoRepository.findBySellerId(sellerId.value()).stream()
                .map(ProductDocument::toDomain).toList();
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return productMongoRepository.findByStatus(status).stream().map(ProductDocument::toDomain).toList();
    }
}
