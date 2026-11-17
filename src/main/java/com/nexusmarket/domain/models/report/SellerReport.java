package com.nexusmarket.domain.models.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * Comportamiento de un vendedor: ventas (ordenes pagadas, despachadas o
 * entregadas), devoluciones aprobadas, reembolsos e inventario de cada uno de
 * sus productos.
 */
public record SellerReport(String sellerId,
                           String sellerName,
                           int productCount,
                           int unitsSold,
                           BigDecimal grossSales,
                           int unitsReturned,
                           BigDecimal refundedAmount,
                           BigDecimal pendingRefundAmount,
                           BigDecimal netSales,
                           int availableUnits,
                           int reservedUnits,
                           List<ProductLine> products) {

    public record ProductLine(String productId,
                              String name,
                              String type,
                              String status,
                              BigDecimal price,
                              int unitsSold,
                              BigDecimal sales,
                              int unitsReturned,
                              BigDecimal refundedAmount,
                              BigDecimal pendingRefundAmount,
                              int availableUnits,
                              int reservedUnits,
                              int damagedUnits) {
    }
}
