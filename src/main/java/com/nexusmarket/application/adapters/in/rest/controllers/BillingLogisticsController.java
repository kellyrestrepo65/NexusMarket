package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.ConsultInvoicesUseCase;
import com.nexusmarket.domain.ports.in.ConsultShipmentsUseCase;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.InvoiceResponse;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ShipmentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** OBJ-09 facturas y OBJ-10 envios, ambos generados por el flujo de la orden. */
@RestController
@RequestMapping("/api")
public class BillingLogisticsController {

    private final ConsultInvoicesUseCase consultInvoicesUseCase;
    private final ConsultShipmentsUseCase consultShipmentsUseCase;

    public BillingLogisticsController(ConsultInvoicesUseCase consultInvoicesUseCase,
                                      ConsultShipmentsUseCase consultShipmentsUseCase) {
        this.consultInvoicesUseCase = consultInvoicesUseCase;
        this.consultShipmentsUseCase = consultShipmentsUseCase;
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultInvoicesUseCase.execute(CurrentUser.of(actor)).stream().map(InvoiceResponse::from).toList();
    }

    @GetMapping("/orders/{orderId}/invoice")
    public InvoiceResponse invoiceOf(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                     @PathVariable String orderId) {
        return InvoiceResponse.from(consultInvoicesUseCase.executeForOrder(CurrentUser.of(actor),
                EntityId.of(orderId)));
    }

    @GetMapping("/shipments")
    public List<ShipmentResponse> shipments(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultShipmentsUseCase.execute(CurrentUser.of(actor)).stream().map(ShipmentResponse::from).toList();
    }

    @GetMapping("/orders/{orderId}/shipment")
    public ShipmentResponse shipmentOf(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                       @PathVariable String orderId) {
        return ShipmentResponse.from(consultShipmentsUseCase.executeForOrder(CurrentUser.of(actor),
                EntityId.of(orderId)));
    }
}
