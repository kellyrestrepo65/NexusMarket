package com.nexusmarket.domain.model.valueobject;

import com.nexusmarket.domain.exception.InvalidArgumentException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * El id de cualquier entidad del dominio: un numero de 6 digitos
 * (100000-999999) en vez de un UUID, para que sea mas facil de leer.
 * {@link #next()} genera ids en memoria de forma secuencial.
 */
public final class EntityId {

    private static final int MIN_VALUE = 100000;
    private static final int MAX_VALUE = 999999;

    private static final AtomicInteger SEQUENCE = new AtomicInteger(MIN_VALUE);

    private final int value;

    public EntityId(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new InvalidArgumentException("The id must have exactly 6 digits (100000-999999)");
        }
        this.value = value;
    }

    public static EntityId next() {
        int value = SEQUENCE.getAndIncrement();
        if (value > MAX_VALUE) {
            throw new IllegalStateException("EntityId sequence exhausted (max 6-digit value reached)");
        }
        return new EntityId(value);
    }

    public static EntityId of(int value) {
        return new EntityId(value);
    }

    public static EntityId of(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidArgumentException("The id is required");
        }
        try {
            return new EntityId(Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("The id must be numeric with exactly 6 digits");
        }
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityId)) return false;
        EntityId entityId = (EntityId) o;
        return value == entityId.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
