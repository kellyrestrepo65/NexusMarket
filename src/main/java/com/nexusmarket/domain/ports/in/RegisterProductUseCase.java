package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.valueobjects.EntityId;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Flujo 6.1 "Catalogo": el vendedor registra sus productos y define sus
 * caracteristicas (tipo y variantes, Dominio 5).
 */
public interface RegisterProductUseCase {

    Product execute(EntityId actorId, String name, BigDecimal price, ProductType type, Map<String, String> variants);
}
