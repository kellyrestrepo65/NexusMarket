package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.RefundStatus;
import com.nexusmarket.domain.enums.ReturnStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.nexusmarket.domain.services.Fixtures.BUYER_ID;
import static com.nexusmarket.domain.services.Fixtures.PRODUCT_ID;
import static com.nexusmarket.domain.services.Fixtures.buyer;
import static com.nexusmarket.domain.services.Fixtures.inventory;
import static com.nexusmarket.domain.services.Fixtures.physicalProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** OBJ-11: solicitud, revision y reembolso de devoluciones. */
@ExtendWith(MockitoExtension.class)
class ReturnServicesTest {

    private static final EntityId ORDER_ID = EntityId.of(200060);
    private static final EntityId RETURN_ID = EntityId.of(200061);
    private static final EntityId ADMIN_ID = EntityId.of(200099);

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private OrderLoader orderLoader;
    @Mock
    private ReturnLoader returnLoader;
    @Mock
    private ReturnRepositoryPort returnRepositoryPort;
    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(ORDER_ID, BUYER_ID);
        order.addItem(new OrderItem(PRODUCT_ID, 2, new BigDecimal("500")));
    }

    private RequestReturnService requestService() {
        return new RequestReturnService(authorizationService, orderLoader, returnRepositoryPort);
    }

    @Test
    void buyerRequestsReturnOfDeliveredOrder() {
        order.advanceTo(OrderStatus.PENDING_PAYMENT);
        order.advanceTo(OrderStatus.PAID);
        order.advanceTo(OrderStatus.DISPATCHED);
        order.advanceTo(OrderStatus.DELIVERED);
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(orderLoader.load(ORDER_ID)).thenReturn(order);
        when(returnRepositoryPort.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());
        when(returnRepositoryPort.save(any())).thenAnswer(i -> i.getArgument(0));

        Return result = requestService().execute(BUYER_ID, ORDER_ID, "Llego roto");

        assertEquals(ReturnStatus.REQUESTED, result.getStatus());
    }

    @Test
    void undeliveredOrderCannotBeReturned() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(orderLoader.load(ORDER_ID)).thenReturn(order);

        assertThrows(BusinessRuleException.class, () -> requestService().execute(BUYER_ID, ORDER_ID, "x"));
        verify(returnRepositoryPort, never()).save(any());
    }

    @Test
    void approvalCreatesRefundAndRestocks() {
        Return orderReturn = new Return(RETURN_ID, ORDER_ID, "Llego roto");
        Inventory stock = inventory(200070, 10);
        int before = stock.getAvailableQuantity();
        when(returnLoader.load(RETURN_ID)).thenReturn(orderReturn);
        when(orderLoader.load(ORDER_ID)).thenReturn(order);
        when(orderLoader.product(any())).thenReturn(physicalProduct());
        when(inventoryRepositoryPort.findByProductId(PRODUCT_ID)).thenReturn(List.of(stock));
        when(returnRepositoryPort.save(orderReturn)).thenReturn(orderReturn);

        Return result = new ReviewReturnService(authorizationService, returnLoader, orderLoader,
                returnRepositoryPort, inventoryRepositoryPort).approve(ADMIN_ID, RETURN_ID);

        verify(authorizationService).requireRole(ADMIN_ID, Role.ADMINISTRATOR);
        assertEquals(ReturnStatus.APPROVED, result.getStatus());
        assertEquals(0, new BigDecimal("1000").compareTo(result.getRefund().getAmount()));
        assertEquals(before + 2, stock.getAvailableQuantity());
        verify(inventoryRepositoryPort).save(stock);
    }

    @Test
    void refundIsProcessed() {
        Return orderReturn = new Return(RETURN_ID, ORDER_ID, "x");
        orderReturn.approve(EntityId.of(200062), new BigDecimal("1000"));
        when(returnLoader.load(RETURN_ID)).thenReturn(orderReturn);
        when(returnRepositoryPort.save(orderReturn)).thenReturn(orderReturn);

        Return result = new ProcessRefundService(authorizationService, returnLoader, returnRepositoryPort)
                .process(ADMIN_ID, RETURN_ID);

        assertEquals(RefundStatus.PROCESSED, result.getRefund().getStatus());
    }

    @Test
    void refundOfUnapprovedReturnFails() {
        when(returnLoader.load(RETURN_ID)).thenReturn(new Return(RETURN_ID, ORDER_ID, "x"));

        assertThrows(BusinessRuleException.class, () -> new ProcessRefundService(authorizationService,
                returnLoader, returnRepositoryPort).process(ADMIN_ID, RETURN_ID));
    }
}
