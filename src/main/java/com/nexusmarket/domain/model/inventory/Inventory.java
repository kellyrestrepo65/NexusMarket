package com.nexusmarket.domain.model.inventory;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.exception.InsufficientInventoryException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.util.Objects;

/**
 * El inventario de un producto en una bodega puntual: cuanto hay
 * disponible y cuanto esta reservado. El stock nunca puede quedar
 * negativo, por eso las cantidades solo cambian a traves de los metodos
 * de movimiento (receiveStock, reserve, releaseReservation,
 * confirmDispatch, adjust), cada uno con su propia validacion.
 */
public class Inventory {

    private final EntityId id;
    private final EntityId productId;
    private final EntityId warehouseId;
    private int availableQuantity;
    private int reservedQuantity;

    public Inventory(EntityId id, EntityId productId, EntityId warehouseId) {
        if (id == null) {
            throw new InvalidArgumentException("The inventory id is required");
        }
        if (productId == null) {
            throw new InvalidArgumentException("The inventory must be linked to a product");
        }
        if (warehouseId == null) {
            throw new InvalidArgumentException("The inventory must be linked to a warehouse");
        }
        this.id = id;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.availableQuantity = 0;
        this.reservedQuantity = 0;
    }

    /** Movimiento de recepcion: entra stock nuevo a la bodega. */
    public void receiveStock(int quantity) {
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity to receive must be greater than zero");
        }
        this.availableQuantity += quantity;
    }

    /**
     * Movimiento de reserva: aparta stock para una orden en curso sin
     * sacarlo todavia de la bodega. Falla si no hay suficiente stock libre
     * (availableQuantity - reservedQuantity &lt; requestedQuantity).
     */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity to reserve must be greater than zero");
        }
        if (getAvailableFreeQuantity() < quantity) {
            throw new InsufficientInventoryException(
                    "Not enough inventory to reserve: available=" + getAvailableFreeQuantity()
                            + ", requested=" + quantity);
        }
        this.reservedQuantity += quantity;
    }

    /** Libera una reserva sin confirmar el despacho (por ejemplo, orden cancelada antes de pagar). */
    public void releaseReservation(int quantity) {
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity to release must be greater than zero");
        }
        if (quantity > this.reservedQuantity) {
            throw new InsufficientInventoryException("Cannot release more than what is reserved");
        }
        this.reservedQuantity -= quantity;
    }

    /** Movimiento de despacho por venta: la reserva se cierra y el producto sale de la bodega. */
    public void confirmDispatch(int quantity) {
        if (quantity <= 0) {
            throw new InvalidArgumentException("The quantity to dispatch must be greater than zero");
        }
        if (quantity > this.reservedQuantity) {
            throw new InsufficientInventoryException("Cannot dispatch more than what is reserved");
        }
        if (quantity > this.availableQuantity) {
            throw new InsufficientInventoryException("Cannot dispatch more than what is available");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity -= quantity;
    }

    /** Movimiento de devolucion: el producto vuelve a quedar disponible en la bodega. */
    public void registerReturn(int quantity) {
        if (quantity <= 0) {
            throw new InvalidArgumentException("The returned quantity must be greater than zero");
        }
        this.availableQuantity += quantity;
    }

    /**
     * Movimiento de ajuste: correccion manual (por ejemplo, producto danado
     * que se detecta en la bodega). Puede ser positivo o negativo, pero
     * nunca deja la cantidad disponible en negativo ni por debajo de lo
     * que ya esta reservado.
     */
    public void adjust(int delta) {
        int newAvailable = this.availableQuantity + delta;
        if (newAvailable < 0) {
            throw new InsufficientInventoryException("The adjustment would leave negative stock");
        }
        if (newAvailable < this.reservedQuantity) {
            throw new InsufficientInventoryException("The adjustment would leave less available than what is already reserved");
        }
        this.availableQuantity = newAvailable;
    }

    public int getAvailableFreeQuantity() {
        return this.availableQuantity - this.reservedQuantity;
    }

    public EntityId getId() {
        return id;
    }

    public EntityId getProductId() {
        return productId;
    }

    public EntityId getWarehouseId() {
        return warehouseId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Inventory)) return false;
        Inventory that = (Inventory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
