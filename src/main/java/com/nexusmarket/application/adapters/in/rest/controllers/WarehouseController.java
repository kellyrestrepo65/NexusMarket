package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.ConsultWarehousesUseCase;
import com.nexusmarket.domain.ports.in.CreateWarehouseUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.CreateWarehouseRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.WarehouseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final ConsultWarehousesUseCase consultWarehousesUseCase;

    public WarehouseController(CreateWarehouseUseCase createWarehouseUseCase,
                               ConsultWarehousesUseCase consultWarehousesUseCase) {
        this.createWarehouseUseCase = createWarehouseUseCase;
        this.consultWarehousesUseCase = consultWarehousesUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse create(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                    @Valid @RequestBody CreateWarehouseRequest request) {
        EntityId sellerId = request.sellerId() == null ? null : EntityId.of(request.sellerId());
        return WarehouseResponse.from(createWarehouseUseCase.execute(CurrentUser.of(actor), request.name(),
                request.type(), sellerId));
    }

    @GetMapping
    public List<WarehouseResponse> consult(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultWarehousesUseCase.execute(CurrentUser.of(actor)).stream()
                .map(WarehouseResponse::from).toList();
    }
}
