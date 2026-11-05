package com.nexusmarket.domain.models.user;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;

/**
 * El supervisor solo consulta reportes y el estado general del sistema,
 * no modifica otros usuarios. Por eso no sobreescribe canOperateOn: se
 * queda con el comportamiento por defecto de User (solo opera sobre si mismo).
 */
public class Supervisor extends User {

    public Supervisor(EntityId id, String identityDocument, String fullName, String email) {
        super(id, identityDocument, fullName, email, Role.SUPERVISOR);
    }
}
