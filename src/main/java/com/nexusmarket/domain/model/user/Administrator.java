package com.nexusmarket.domain.model.user;

import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.Role;

/**
 * El administrador maneja vendedores y bodegas. Es el unico que puede
 * registrar un vendedor nuevo (ver Seller, no tiene auto-registro), por
 * eso aqui se sobreescribe canOperateOn para que tambien pueda operar
 * sobre vendedores, no solo sobre si mismo.
 */
public class Administrator extends User {

    public Administrator(EntityId id, String fullName, String email) {
        super(id, fullName, email, Role.ADMINISTRATOR);
    }

    @Override
    public boolean canOperateOn(User other) {
        return other instanceof Seller || super.canOperateOn(other);
    }
}
