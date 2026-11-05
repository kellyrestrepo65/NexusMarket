package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.nexusmarket.domain.services.Fixtures.BUYER_ID;
import static com.nexusmarket.domain.services.Fixtures.PRODUCT_ID;
import static com.nexusmarket.domain.services.Fixtures.buyer;
import static com.nexusmarket.domain.services.Fixtures.digitalProduct;
import static com.nexusmarket.domain.services.Fixtures.inventory;
import static com.nexusmarket.domain.services.Fixtures.physicalProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Flujo 6.1 "Compra": creacion de la orden y reserva del inventario. */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ShoppingCartRepositoryPort shoppingCartRepositoryPort;
    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;
    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @InjectMocks
    private CheckoutService service;

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart(EntityId.of(200099), BUYER_ID);
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(shoppingCartRepositoryPort.findByBuyerId(BUYER_ID)).thenReturn(Optional.of(cart));
    }

    @Test
    void reservesStockAcrossWarehousesAndClearsCart() {
        cart.addProduct(PRODUCT_ID, 5);
        Inventory first = inventory(1, 3);
        Inventory second = inventory(2, 4);
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(inventoryRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of(first, second));
        when(orderRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.execute(BUYER_ID);

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(0, new BigDecimal("250000").compareTo(order.calculateTotal()));
        assertEquals(3, first.getReservedQuantity());
        assertEquals(2, second.getReservedQuantity());
        assertTrue(cart.isEmpty());
        verify(shoppingCartRepositoryPort).save(cart);
    }

    @Test
    void failsWhenStockIsInsufficient() {
        cart.addProduct(PRODUCT_ID, 10);
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(inventoryRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of(inventory(1, 3)));

        assertThrows(BusinessRuleException.class, () -> service.execute(BUYER_ID));
        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void damagedUnitsAreNotReserved() {
        cart.addProduct(PRODUCT_ID, 3);
        Inventory inventory = inventory(1, 3);
        inventory.markAsDamaged(1);
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(inventoryRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of(inventory));

        assertThrows(BusinessRuleException.class, () -> service.execute(BUYER_ID));
    }

    @Test
    void digitalProductsDoNotTouchInventory() {
        cart.addProduct(PRODUCT_ID, 1);
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(digitalProduct()));
        when(orderRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.execute(BUYER_ID);

        verify(inventoryRepositoryPort, never()).findByProductId(any());
    }

    @Test
    void emptyCartCannotBeConfirmed() {
        assertThrows(BusinessRuleException.class, () -> service.execute(BUYER_ID));
    }
}
