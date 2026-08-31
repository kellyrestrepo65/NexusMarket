package com.nexusmarket.domain.model.valueobject;

import com.nexusmarket.domain.exception.InvalidArgumentException;

import java.util.Objects;

/**
 * Una direccion fisica. No tiene id propio, dos direcciones son iguales
 * si tienen los mismos datos, no si son el mismo objeto.
 */
public final class Address {

    private final String addressLine1;
    private final String city;
    private final String stateOrProvince;
    private final String country;
    private final String postalCode;

    public Address(String addressLine1, String city, String stateOrProvince,
                    String country, String postalCode) {
        if (addressLine1 == null || addressLine1.isBlank()) {
            throw new InvalidArgumentException("The address line 1 is required");
        }
        if (city == null || city.isBlank()) {
            throw new InvalidArgumentException("The address city is required");
        }
        if (country == null || country.isBlank()) {
            throw new InvalidArgumentException("The address country is required");
        }
        this.addressLine1 = addressLine1;
        this.city = city;
        this.stateOrProvince = stateOrProvince;
        this.country = country;
        this.postalCode = postalCode;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getCity() {
        return city;
    }

    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address that = (Address) o;
        return Objects.equals(addressLine1, that.addressLine1)
                && Objects.equals(city, that.city)
                && Objects.equals(stateOrProvince, that.stateOrProvince)
                && Objects.equals(country, that.country)
                && Objects.equals(postalCode, that.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressLine1, city, stateOrProvince, country, postalCode);
    }

    @Override
    public String toString() {
        return addressLine1 + ", " + city + ", " + country;
    }
}
