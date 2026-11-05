package com.nexusmarket.domain.services.cart;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.models.cart.ShoppingCart;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.user.Buyer;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.ShoppingCartRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nexusmarket.domain.services.Fixtures.BUYER_ID;
import static com.nexusmarket.domain.services.Fixtures.PRODUCT_ID;
import static com.nexusmarket.domain.services.Fixtures.buyer;
import static com.nexusmarket.domain.services.Fixtures.physicalProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductToCartServiceTest {

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private ShoppingCartRepositoryPort shoppingCartRepositoryPort;

    @InjectMocks
    private AddProductToCartService service;

    @Test
    void createsCartOnFirstUse() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(shoppingCartRepositoryPort.findByBuyerId(BUYER_ID)).thenReturn(Optional.empty());
        when(shoppingCartRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ShoppingCart cart = service.execute(BUYER_ID, PRODUCT_ID, 2);

        assertEquals(BUYER_ID, cart.getBuyerId());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void restrictedBuyerCannotBuy() {
        Buyer buyer = buyer();
        buyer.restrict();
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer);

        assertThrows(BusinessRuleException.class, () -> service.execute(BUYER_ID, PRODUCT_ID, 1));
        verify(shoppingCartRepositoryPort, never()).save(any());
    }

    @Test
    void suspendedProductCannotBeAdded() {
        Product product = physicalProduct();
        product.suspend();
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        assertThrows(BusinessRuleException.class, () -> service.execute(BUYER_ID, PRODUCT_ID, 1));
    }

    @Test
    void unknownProductIsNotFound() {
        when(authorizationService.requireRole(BUYER_ID, Role.BUYER)).thenReturn(buyer());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.execute(BUYER_ID, PRODUCT_ID, 1));
    }
}
