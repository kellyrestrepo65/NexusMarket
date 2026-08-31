package com.nexusmarket.domain.model.user;

import com.nexusmarket.domain.model.valueobject.EntityId;
import com.nexusmarket.domain.model.valueobject.Role;

/**
 * El operador logistico se encarga de la operacion fisica de bodegas y
 * despachos. No tiene atributos propios, solo el rol lo distingue de
 * los demas usuarios.
 */
public class LogisticsOperator extends User {

    public LogisticsOperator(EntityId id, String fullName, String email) {
        super(id, fullName, email, Role.LOGISTICS_OPERATOR);
    }
}
