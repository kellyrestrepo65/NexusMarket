package com.nexusmarket.domain.ports.in;

import com.nexusmarket.domain.models.report.SellerReport;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.util.List;

/**
 * Reportes de vendedores: el Administrador y el Supervisor consultan todos; el
 * Vendedor solo el suyo. Los demas roles no tienen acceso.
 */
public interface SellerReportUseCase {

    /** Solo Administrador y Supervisor. */
    List<SellerReport> all(EntityId actorId);

    /** Solo Vendedor: su propio reporte. */
    SellerReport mine(EntityId actorId);

    /** Administrador/Supervisor cualquier vendedor; Vendedor solo el suyo. */
    SellerReport of(EntityId actorId, EntityId sellerId);

    /** Lo que el actor puede ver: todos (Admin/Supervisor) o el suyo (Vendedor). */
    List<SellerReport> visibleTo(EntityId actorId);
}
