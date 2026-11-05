package com.nexusmarket.domain.services.inventory;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.inventory.Warehouse;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.nexusmarket.domain.services.Fixtures.LOGISTICS_ID;
import static com.nexusmarket.domain.services.Fixtures.PRODUCT_ID;
import static com.nexusmarket.domain.services.Fixtures.WAREHOUSE_ID;
import static com.nexusmarket.domain.services.Fixtures.digitalProduct;
import static com.nexusmarket.domain.services.Fixtures.inventory;
import static com.nexusmarket.domain.services.Fixtures.logistics;
import static com.nexusmarket.domain.services.Fixtures.physicalProduct;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Dominio 6 "Ingreso" y la matriz de responsabilidades de inventario. */
@ExtendWith(MockitoExtension.class)
class ReceiveStockServiceTest {

    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private WarehouseRepositoryPort warehouseRepositoryPort;
    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;
    @Mock
    private Warehouse warehouse;

    private ReceiveStockService service;

    @BeforeEach
    void setUp() {
        InventoryAccessValidator validator = new InventoryAccessValidator(authorizationService,
                productRepositoryPort, warehouseRepositoryPort);
        service = new ReceiveStockService(validator, inventoryRepositoryPort);
        when(warehouseRepositoryPort.findById(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse));
    }

    @Test
    void createsInventoryWithFirstEntry() {
        when(authorizationService.requireRole(LOGISTICS_ID, Role.SELLER, Role.LOGISTICS_OPERATOR))
                .thenReturn(logistics());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(inventoryRepositoryPort.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Inventory inventory = service.execute(LOGISTICS_ID, PRODUCT_ID, WAREHOUSE_ID, 10);

        assertEquals(10, inventory.getAvailableQuantity());
    }

    @Test
    void addsToExistingInventory() {
        Inventory existing = inventory(1, 5);
        when(authorizationService.requireRole(LOGISTICS_ID, Role.SELLER, Role.LOGISTICS_OPERATOR))
                .thenReturn(logistics());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));
        when(inventoryRepositoryPort.findByProductIdAndWarehouseId(PRODUCT_ID, WAREHOUSE_ID))
                .thenReturn(Optional.of(existing));
        when(inventoryRepositoryPort.save(existing)).thenReturn(existing);

        assertEquals(8, service.execute(LOGISTICS_ID, PRODUCT_ID, WAREHOUSE_ID, 3).getAvailableQuantity());
    }

    @Test
    void digitalProductsHaveNoInventory() {
        when(authorizationService.requireRole(LOGISTICS_ID, Role.SELLER, Role.LOGISTICS_OPERATOR))
                .thenReturn(logistics());
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(digitalProduct()));

        assertThrows(BusinessRuleException.class,
                () -> service.execute(LOGISTICS_ID, PRODUCT_ID, WAREHOUSE_ID, 1));
        verify(inventoryRepositoryPort, never()).save(any());
    }

    @Test
    void sellerCannotUseAnotherSellersProduct() {
        EntityId otherSeller = EntityId.of(200088);
        User seller = mock(User.class);
        when(seller.getRole()).thenReturn(Role.SELLER);
        when(seller.getId()).thenReturn(otherSeller);
        when(authorizationService.requireRole(otherSeller, Role.SELLER, Role.LOGISTICS_OPERATOR))
                .thenReturn(seller);
        when(productRepositoryPort.findById(PRODUCT_ID)).thenReturn(Optional.of(physicalProduct()));

        assertThrows(ForbiddenOperationException.class,
                () -> service.execute(otherSeller, PRODUCT_ID, WAREHOUSE_ID, 1));
    }
}
