package com.nexusmarket.application.adapters.out.persistence.mongodb.repositories;

import com.nexusmarket.application.adapters.out.persistence.mongodb.documents.ProductDocument;

import com.nexusmarket.domain.enums.ProductStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductMongoRepository extends MongoRepository<ProductDocument, Integer> {

    List<ProductDocument> findBySellerId(Integer sellerId);

    List<ProductDocument> findByStatus(ProductStatus status);
}
