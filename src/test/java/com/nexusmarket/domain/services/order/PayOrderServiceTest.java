package com.nexusmarket.domain.services.order;

import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.exceptions.InvalidStateTransitionException;
import com.nexusmarket.domain.models.order.Invoice;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.order.Shipment;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.valueobjects.Address;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.enums.ShipmentStatus;
import com.nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ShipmentRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static com.nexusmarket.domain.services.Fixtures.BUYER_ID;
import static com.nexusmarket.domain.services.Fixtures.PRODUCT_ID;
import static com.nexusmarket.domain.services.Fixtures.buyer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Flujo 6.1 "Pago": solo el dueno paga, y solo una orden pendiente. */
@ExtendWith(MockitoExtension.class)
class PayOrderServiceTest {

    private static final EntityId ORDER_ID = EntityId.of(200050);

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private OrderLoader orderLoader;
    @Mock
    private OrderRepositoryPort orderRepositoryPort;
    @Mock
    private InvoiceRepositoryPort invoiceRepositoryPort;
    @Mock
    private ShipmentRepositoryPort shipmentRepositoryPort;

    @InjectMocks
    private PayOrderService service;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(ORDER_ID, BUYER_ID);
        order.addItem(new OrderItem(PRODUCT_ID, 1, new BigDecimal("1000")));
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        when(orderLoader.load(ORDER_ID)).thenReturn(order);
    }

    @Test
    void physicalOrderStaysPaid() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(orderLoader.isOnlyDigital(order)).thenReturn(false);
        when(orderRepositoryPort.save(order)).thenReturn(order);

        assertEquals(OrderStatus.PAID, service.execute(BUYER_ID, ORDER_ID).getStatus());
        ArgumentCaptor<Invoice> invoice = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepositoryPort).save(invoice.capture());
        assertEquals(ORDER_ID, invoice.getValue().getOrderId());
        assertEquals(0, new BigDecimal("1000").compareTo(invoice.getValue().getTotal()));
        ArgumentCaptor<Shipment> shipment = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepositoryPort).save(shipment.capture());
        assertEquals(ShipmentStatus.IN_PREPARATION, shipment.getValue().getStatus());
    }

    @Test
    void digitalOrderIsDeliveredRightAway() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(orderLoader.isOnlyDigital(order)).thenReturn(true);
        when(orderRepositoryPort.save(order)).thenReturn(order);

        assertEquals(OrderStatus.DELIVERED, service.execute(BUYER_ID, ORDER_ID).getStatus());
        verify(invoiceRepositoryPort).save(any());
        verify(shipmentRepositoryPort, never()).save(any());
    }

    @Test
    void anotherBuyerCannotPay() {
        EntityId otherId = EntityId.of(200077);
        Buyer other = new Buyer(otherId, "999", "Otro", "otro@mail.com",
                new Address("Calle 2", "Cali", null, "CO", null));
        when(authorizationService.requireRole(otherId, Role.BUYER)).thenReturn(other);

        assertThrows(ForbiddenOperationException.class, () -> service.execute(otherId, ORDER_ID));
        verify(orderRepositoryPort, never()).save(any());
        verify(invoiceRepositoryPort, never()).save(any());
    }

    @Test
    void cannotPayTwice() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        order.advanceTo(OrderStatus.PAID);

        assertThrows(InvalidStateTransitionException.class, () -> service.execute(BUYER_ID, ORDER_ID));
    }
}
