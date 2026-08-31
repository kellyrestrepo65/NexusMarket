package com.nexusmarket.domain.model.user;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.Address;
import com.nexusmarket.domain.model.valueobject.CommercialStatus;
import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Un comprador. Tiene una direccion principal y puede agregar direcciones
 * adicionales. El comercialStatus controla si puede comprar o no (ver
 * canPurchase); un comprador restringido no puede confirmar ordenes.
 */
public class Buyer extends User {

    private Address mainAddress;
    private final List<Address> additionalAddresses;
    private CommercialStatus commercialStatus;

    public Buyer(EntityId id, String fullName, String email, Address mainAddress) {
        super(id, fullName, email, Role.BUYER);
        if (mainAddress == null) {
            throw new InvalidArgumentException("The main address is required for a buyer");
        }
        this.mainAddress = mainAddress;
        this.additionalAddresses = new ArrayList<>();
        this.commercialStatus = CommercialStatus.ENABLED;
    }

    public void addAdditionalAddress(Address address) {
        if (address == null) {
            throw new InvalidArgumentException("The address cannot be null");
        }
        this.additionalAddresses.add(address);
    }

    public void changeMainAddress(Address newAddress) {
        if (newAddress == null) {
            throw new InvalidArgumentException("The main address cannot be null");
        }
        this.mainAddress = newAddress;
    }

    public void restrict() {
        this.commercialStatus = CommercialStatus.RESTRICTED;
    }

    public void enable() {
        this.commercialStatus = CommercialStatus.ENABLED;
    }

    /** Se necesita estar activo y con comercialStatus ENABLED para poder confirmar una orden. */
    public boolean canPurchase() {
        return isActive() && commercialStatus == CommercialStatus.ENABLED;
    }

    public Address getMainAddress() {
        return mainAddress;
    }

    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public CommercialStatus getCommercialStatus() {
        return commercialStatus;
    }
}
