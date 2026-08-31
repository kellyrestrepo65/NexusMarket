package com.nexusmarket.domain.model.catalog;

import com.nexusmarket.domain.exception.InvalidArgumentException;
import com.nexusmarket.domain.model.valueobject.EntityId;

import java.util.Objects;

/** Una variante de un producto, por ejemplo color: rojo o talla: M. */
public class Variant {

    private final EntityId id;
    private final String attribute;
    private final String value;

    public Variant(EntityId id, String attribute, String value) {
        if (id == null) {
            throw new InvalidArgumentException("The variant id is required");
        }
        if (attribute == null || attribute.isBlank()) {
            throw new InvalidArgumentException("The variant attribute cannot be empty");
        }
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("The variant value cannot be empty");
        }
        this.id = id;
        this.attribute = attribute;
        this.value = value;
    }

    public EntityId getId() {
        return id;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variant)) return false;
        Variant variant = (Variant) o;
        return Objects.equals(id, variant.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return attribute + ": " + value;
    }
}
