package com.nexusmarket.domain.models.report;

import java.math.BigDecimal;
import java.util.Map;

/**
 * OBJ-12: informacion administrativa consolidada. Cada mapa cuenta los registros
 * por estado; las unidades disponibles se suman por id de bodega.
 */
public record AdministrativeReport(Map<String, Long> ordersByStatus,
                                   long invoiceCount,
                                   BigDecimal totalInvoiced,
                                   Map<String, Long> shipmentsByStatus,
                                   Map<String, Long> returnsByStatus,
                                   BigDecimal totalRefunded,
                                   Map<String, Integer> availableUnitsByWarehouse) {
}
