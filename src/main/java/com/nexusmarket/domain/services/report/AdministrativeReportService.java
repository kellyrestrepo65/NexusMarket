package com.nexusmarket.domain.services.report;

import com.nexusmarket.domain.models.user.Administrator;
import com.nexusmarket.domain.models.user.Supervisor;

import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Refund;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.report.AdministrativeReport;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.RefundStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.AdministrativeReportUseCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OBJ-12 "Consolidar informacion administrativa": el Administrador y el
 * Supervisor consultan ordenes, facturacion, logistica, devoluciones e
 * inventario en un solo reporte. Solo los reembolsos procesados cuentan como
 * dinero reembolsado.
 */
@Service
public class AdministrativeReportService implements AdministrativeReportUseCase {

    private final AuthorizationService authorizationService;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ShipmentRepositoryPort shipmentRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public AdministrativeReportService(AuthorizationService authorizationService,
                                       OrderRepositoryPort orderRepositoryPort,
                                       InvoiceRepositoryPort invoiceRepositoryPort,
                                       ShipmentRepositoryPort shipmentRepositoryPort,
                                       ReturnRepositoryPort returnRepositoryPort,
                                       InventoryRepositoryPort inventoryRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderRepositoryPort = orderRepositoryPort;
        this.invoiceRepositoryPort = invoiceRepositoryPort;
        this.shipmentRepositoryPort = shipmentRepositoryPort;
        this.returnRepositoryPort = returnRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Override
    public AdministrativeReport execute(EntityId actorId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR, Role.SUPERVISOR);
        List<Invoice> invoices = invoiceRepositoryPort.findAll();
        List<Return> returns = returnRepositoryPort.findAll();
        BigDecimal totalInvoiced = invoices.stream().map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefunded = returns.stream()
                .map(Return::getRefund)
                .filter(Objects::nonNull)
                .filter(refund -> refund.getStatus() == RefundStatus.PROCESSED)
                .map(Refund::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Integer> unitsByWarehouse = inventoryRepositoryPort.findAll().stream()
                .collect(Collectors.groupingBy(inventory -> inventory.getWarehouseId().toString(), TreeMap::new,
                        Collectors.summingInt(Inventory::getAvailableQuantity)));
        return new AdministrativeReport(
                countBy(orderRepositoryPort.findAll(), order -> order.getStatus().name()),
                invoices.size(),
                totalInvoiced,
                countBy(shipmentRepositoryPort.findAll(), shipment -> shipment.getStatus().name()),
                countBy(returns, orderReturn -> orderReturn.getStatus().name()),
                totalRefunded,
                unitsByWarehouse);
    }

    private static <T> Map<String, Long> countBy(List<T> items, Function<T, String> status) {
        return items.stream().collect(Collectors.groupingBy(status, TreeMap::new, Collectors.counting()));
    }
}
