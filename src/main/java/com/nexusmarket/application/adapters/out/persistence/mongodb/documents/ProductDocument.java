package com.nexusmarket.application.adapters.out.persistence.mongodb.documents;

import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.catalog.Variant;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * El catalogo vive en MongoDB porque cada producto tiene sus propias
 * caracteristicas variables (variantes), que encajan mejor en un documento que
 * en una tabla.
 */
@Document(collection = "products")
public class ProductDocument {

    @Id
    private Integer id;

    @Indexed
    private Integer sellerId;

    private String name;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    private ProductType type;

    @Indexed
    private ProductStatus status;

    private List<VariantDocument> variants = new ArrayList<>();

    public record VariantDocument(Integer id, String attribute, String value) {
    }

    public static ProductDocument from(Product product) {
        ProductDocument document = new ProductDocument();
        document.id = product.getId().value();
        document.sellerId = product.getSellerId().value();
        document.name = product.getName();
        document.price = product.getPrice();
        document.type = product.getType();
        document.status = product.getStatus();
        for (Variant variant : product.getVariants()) {
            document.variants.add(new VariantDocument(variant.getId().value(), variant.getAttribute(), variant.getValue()));
        }
        return document;
    }

    public Product toDomain() {
        Product product = new Product(EntityId.of(id), EntityId.of(sellerId), name, price, type);
        for (VariantDocument variant : variants) {
            product.addVariant(new Variant(EntityId.of(variant.id()), variant.attribute(), variant.value()));
        }
        switch (status) {
            case PUBLISHED -> product.publish();
            case SUSPENDED -> product.suspend();
            case DISCONTINUED -> product.discontinue();
        }
        return product;
    }

    public Integer getId() {
        return id;
    }

    public List<VariantDocument> getVariants() {
        return variants;
    }
}
