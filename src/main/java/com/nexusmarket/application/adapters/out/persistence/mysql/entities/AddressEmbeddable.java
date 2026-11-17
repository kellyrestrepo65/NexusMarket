package com.nexusmarket.application.adapters.out.persistence.mysql.entities;

import com.nexusmarket.domain.valueobjects.Address;
import jakarta.persistence.Embeddable;

@Embeddable
public class AddressEmbeddable {

    private String addressLine1;
    private String city;
    private String stateOrProvince;
    private String country;
    private String postalCode;

    protected AddressEmbeddable() {
    }

    public static AddressEmbeddable from(Address address) {
        if (address == null) {
            return null;
        }
        AddressEmbeddable embeddable = new AddressEmbeddable();
        embeddable.addressLine1 = address.getAddressLine1();
        embeddable.city = address.getCity();
        embeddable.stateOrProvince = address.getStateOrProvince();
        embeddable.country = address.getCountry();
        embeddable.postalCode = address.getPostalCode();
        return embeddable;
    }

    public Address toDomain() {
        return new Address(addressLine1, city, stateOrProvince, country, postalCode);
    }
}
