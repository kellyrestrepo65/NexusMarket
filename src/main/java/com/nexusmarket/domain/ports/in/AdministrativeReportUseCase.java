package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.report.AdministrativeReport;
import com.nexusmarket.domain.valueobjects.EntityId;

/**
 * OBJ-12 "Consolidar informacion administrativa": el Administrador y el
 * Supervisor consultan ordenes, facturacion, logistica, devoluciones e
 * inventario en un solo reporte. Solo los reembolsos procesados cuentan como
 * dinero reembolsado.
 */
public interface AdministrativeReportUseCase {

    AdministrativeReport execute(EntityId actorId);
}
