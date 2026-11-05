package com.nexusmarket.application.adapters.in.rest.responses;

import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Refund;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Cuerpos de respuesta de la API REST; los objetos del dominio nunca se exponen directamente. */
public final class Responses {

    private Responses() {
    }

    private static String text(EntityId id) {
        return id == null ? null : id.toString();
    }

    public record UserResponse(String id, String identityDocument, String fullName, String email,
                               String role, String status, String commercialStatus, Address mainAddress) {
        public static UserResponse from(User user) {
            Buyer buyer = user instanceof Buyer b ? b : null;
            return new UserResponse(text(user.getId()), user.getIdentityDocument(), user.getFullName(),
                    user.getEmail(), user.getRole().name(), user.getStatus().name(),
                    buyer == null ? null : buyer.getCommercialStatus().name(),
                    buyer == null ? null : buyer.getMainAddress());
        }
    }

    public record WarehouseResponse(String id, String name, String type, String ownerId) {
        public static WarehouseResponse from(Warehouse warehouse) {
            return new WarehouseResponse(text(warehouse.getId()), warehouse.getName(),
                    warehouse.getType().name(), text(warehouse.getOwnerId()));
        }
    }

    public record VariantResponse(String id, String attribute, String value) {
    }

    public record ProductResponse(String id, String sellerId, String name, BigDecimal price,
                                  String type, String status, List<VariantResponse> variants) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(text(product.getId()), text(product.getSellerId()), product.getName(),
                    product.getPrice(), product.getType().name(), product.getStatus().name(),
                    product.getVariants().stream()
                            .map(v -> new VariantResponse(text(v.getId()), v.getAttribute(), v.getValue()))
                            .toList());
        }
    }

    public record InventoryResponse(String id, String productId, String warehouseId, int available,
                                    int reserved, int damaged, int free) {
        public static InventoryResponse from(Inventory inventory) {
            return new InventoryResponse(text(inventory.getId()), text(inventory.getProductId()),
                    text(inventory.getWarehouseId()), inventory.getAvailableQuantity(),
                    inventory.getReservedQuantity(), inventory.getDamagedQuantity(),
                    inventory.getAvailableFreeQuantity());
        }
    }

    public record ItemResponse(String productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }

    public record CartResponse(String id, String buyerId, List<ItemResponse> items) {
        public static CartResponse from(ShoppingCart cart) {
            return new CartResponse(text(cart.getId()), text(cart.getBuyerId()), cart.getItems().stream()
                    .map(i -> new ItemResponse(text(i.getProductId()), i.getQuantity(), null, null))
                    .toList());
        }
    }

    public record OrderResponse(String id, String buyerId, String status, BigDecimal total,
                                List<ItemResponse> items) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(text(order.getId()), text(order.getBuyerId()), order.getStatus().name(),
                    order.calculateTotal(), order.getItems().stream()
                    .map(i -> new ItemResponse(text(i.getProductId()), i.getQuantity(),
                            i.getUnitPrice(), i.getSubtotal()))
                    .toList());
        }
    }

    public record InvoiceResponse(String id, String orderId, BigDecimal total, LocalDateTime issueDate) {
        public static InvoiceResponse from(Invoice invoice) {
            return new InvoiceResponse(text(invoice.getId()), text(invoice.getOrderId()), invoice.getTotal(),
                    invoice.getIssueDate());
        }
    }

    public record ShipmentResponse(String id, String orderId, String logisticsOperatorId, String status) {
        public static ShipmentResponse from(Shipment shipment) {
            return new ShipmentResponse(text(shipment.getId()), text(shipment.getOrderId()),
                    text(shipment.getLogisticsOperatorId()), shipment.getStatus().name());
        }
    }

    public record RefundResponse(String id, BigDecimal amount, String status) {
    }

    public record ReturnResponse(String id, String orderId, String reason, String status, RefundResponse refund) {
        public static ReturnResponse from(Return orderReturn) {
            Refund refund = orderReturn.getRefund();
            return new ReturnResponse(text(orderReturn.getId()), text(orderReturn.getOrderId()),
                    orderReturn.getReason(), orderReturn.getStatus().name(), refund == null ? null
                    : new RefundResponse(text(refund.getId()), refund.getAmount(), refund.getStatus().name()));
        }
    }

    public record ErrorResponse(int status, String error, String message) {
    }
}
