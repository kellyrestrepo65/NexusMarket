package com.nexusmarket.application.adapters.in.rest.controllers;

import com.nexusmarket.infrastructure.security.CurrentUser;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.ports.in.ChangeProductStatusUseCase;
import com.nexusmarket.domain.ports.in.ConsultCatalogUseCase;
import com.nexusmarket.domain.ports.in.RegisterProductUseCase;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.ChangeProductStatusRequest;
import com.nexusmarket.application.adapters.in.rest.requests.Requests.RegisterProductRequest;
import com.nexusmarket.application.adapters.in.rest.responses.Responses.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final RegisterProductUseCase registerProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final ConsultCatalogUseCase consultCatalogUseCase;

    public ProductController(RegisterProductUseCase registerProductUseCase,
                             ChangeProductStatusUseCase changeProductStatusUseCase,
                             ConsultCatalogUseCase consultCatalogUseCase) {
        this.registerProductUseCase = registerProductUseCase;
        this.changeProductStatusUseCase = changeProductStatusUseCase;
        this.consultCatalogUseCase = consultCatalogUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse register(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                    @Valid @RequestBody RegisterProductRequest request) {
        return ProductResponse.from(registerProductUseCase.execute(CurrentUser.of(actor), request.name(),
                request.price(), request.type(), request.variants()));
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse changeStatus(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor,
                                        @PathVariable String productId,
                                        @Valid @RequestBody ChangeProductStatusRequest request) {
        return ProductResponse.from(changeProductStatusUseCase.execute(CurrentUser.of(actor),
                EntityId.of(productId), request.status()));
    }

    /** Catalogo publico: solo productos publicados. */
    @GetMapping
    public List<ProductResponse> catalog(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultCatalogUseCase.executePublished(CurrentUser.of(actor)).stream()
                .map(ProductResponse::from).toList();
    }

    /** The seller's own products, whatever their status. */
    @GetMapping("/mine")
    public List<ProductResponse> own(@RequestHeader(value = CurrentUser.HEADER, required = false) String actor) {
        return consultCatalogUseCase.executeOwn(CurrentUser.of(actor)).stream()
                .map(ProductResponse::from).toList();
    }
}
