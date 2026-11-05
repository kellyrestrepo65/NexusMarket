package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.AdjustInventoryUseCase;
import com.nexusmarket.domain.ports.in.ConsultInventoryUseCase;
import com.nexusmarket.domain.ports.in.MarkInventoryDamagedUseCase;
import com.nexusmarket.domain.ports.in.ReceiveStockUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.AdjustInventoryRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.QuantityRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.ReceiveStockRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.InventoryResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final ReceiveStockUseCase receiveStockUseCase;
    private final AdjustInventoryUseCase adjustInventoryUseCase;
    private final MarkInventoryDamagedUseCase markInventoryDamagedUseCase;
    private final ConsultInventoryUseCase consultInventoryUseCase;

    public InventoryController(ReceiveStockUseCase receiveStockUseCase, AdjustInventoryUseCase adjustInventoryUseCase,
                               MarkInventoryDamagedUseCase markInventoryDamagedUseCase,
                               ConsultInventoryUseCase consultInventoryUseCase) {
        this.receiveStockUseCase = receiveStockUseCase;
        this.adjustInventoryUseCase = adjustInventoryUseCase;
        this.markInventoryDamagedUseCase = markInventoryDamagedUseCase;
        this.consultInventoryUseCase = consultInventoryUseCase;
    }

    @PostMapping("/receipts")
    public InventoryResponse receive(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                     @Valid @RequestBody ReceiveStockRequest request) {
        return InventoryResponse.from(receiveStockUseCase.execute(CurrentUser.of(actor),
                EntityId.of(request.productId()), EntityId.of(request.warehouseId()), request.quantity()));
    }

    @PostMapping("/{inventoryId}/adjustments")
    public InventoryResponse adjust(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                    @PathVariable String inventoryId,
                                    @RequestBody AdjustInventoryRequest request) {
        return InventoryResponse.from(adjustInventoryUseCase.execute(CurrentUser.of(actor),
                EntityId.of(inventoryId), request.delta()));
    }

    @PostMapping("/{inventoryId}/damaged")
    public InventoryResponse markDamaged(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                         @PathVariable String inventoryId,
                                         @Valid @RequestBody QuantityRequest request) {
        return InventoryResponse.from(markInventoryDamagedUseCase.execute(CurrentUser.of(actor),
                EntityId.of(inventoryId), request.quantity()));
    }

    @GetMapping
    public List<InventoryResponse> consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                           @RequestParam String productId) {
        return consultInventoryUseCase.execute(CurrentUser.of(actor), EntityId.of(productId)).stream()
                .map(InventoryResponse::from).toList();
    }
}
