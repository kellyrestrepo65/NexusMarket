package com.nexusmarket.application.adapters.in.rest.requests;

import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.enums.ProductStatus;
import com.nexusmarket.domain.enums.ProductType;
import com.nexusmarket.domain.enums.WarehouseType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Map;

/** Cuerpos de peticion de la API REST. Las reglas de negocio se quedan en el dominio. */
public final class Requests {

    private Requests() {
    }

    public record AddressRequest(@NotBlank String addressLine1, @NotBlank String city,
                                 String stateOrProvince, @NotBlank String country, String postalCode) {
        public Address toDomain() {
            return new Address(addressLine1, city, stateOrProvince, country, postalCode);
        }
    }

    public record RegisterBuyerRequest(@NotBlank String identityDocument, @NotBlank String fullName,
                                       @NotBlank @Email String email,
                                       @NotNull @Valid AddressRequest mainAddress) {
    }

    public record RegisterSellerRequest(@NotBlank String identityDocument, @NotBlank String fullName,
                                        @NotBlank @Email String email, @NotBlank String firstWarehouseName) {
    }

    public record ChangeUserStatusRequest(@NotNull Boolean active) {
    }

    public record CreateWarehouseRequest(@NotBlank String name, @NotNull WarehouseType type, String sellerId) {
    }

    public record RegisterProductRequest(@NotBlank String name, @NotNull @PositiveOrZero BigDecimal price,
                                         @NotNull ProductType type, Map<String, String> variants) {
    }

    public record ChangeProductStatusRequest(@NotNull ProductStatus status) {
    }

    public record ReceiveStockRequest(@NotBlank String productId, @NotBlank String warehouseId,
                                      @Positive int quantity) {
    }

    public record AdjustInventoryRequest(int delta) {
    }

    public record QuantityRequest(@Positive int quantity) {
    }

    public record AddToCartRequest(@NotBlank String productId, @Positive int quantity) {
    }

    public record RequestReturnRequest(@NotBlank String orderId, @NotBlank String reason) {
    }
}
