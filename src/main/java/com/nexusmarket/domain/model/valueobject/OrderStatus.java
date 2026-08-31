package com.nexusmarket.domain.model.valueobject;

/**
 * Los estados posibles de una orden, en orden:
 * CART -> PENDING_PAYMENT -> PAID -> DISPATCHED -> DELIVERED.
 * canAdvanceTo() dice a que estado se puede pasar desde cada uno: siempre
 * el siguiente, nunca saltando pasos ni hacia atras. DELIVERED no puede
 * avanzar a ningun lado, una orden entregada ya no cambia.
 */
public enum OrderStatus {
    CART {
        @Override
        public boolean canAdvanceTo(OrderStatus next) {
            return next == PENDING_PAYMENT;
        }
    },
    PENDING_PAYMENT {
        @Override
        public boolean canAdvanceTo(OrderStatus next) {
            return next == PAID;
        }
    },
    PAID {
        @Override
        public boolean canAdvanceTo(OrderStatus next) {
            return next == DISPATCHED;
        }
    },
    DISPATCHED {
        @Override
        public boolean canAdvanceTo(OrderStatus next) {
            return next == DELIVERED;
        }
    },
    DELIVERED {
        @Override
        public boolean canAdvanceTo(OrderStatus next) {
            return false;
        }
    };

    public abstract boolean canAdvanceTo(OrderStatus next);

    public boolean isFinal() {
        return this == DELIVERED;
    }
}
