package com.nexusmarket.domain.model.user;

import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.Role;

/**
 * El supervisor solo consulta reportes y el estado general del sistema,
 * no modifica otros usuarios. Por eso no sobreescribe canOperateOn: se
 * queda con el comportamiento por defecto de User (solo opera sobre si mismo).
 */
public class Supervisor extends User {

    public Supervisor(EntityId id, String fullName, String email) {
        super(id, fullName, email, Role.SUPERVISOR);
    }
}
