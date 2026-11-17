package com.nexusmarket.domain.models.user;

import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;

/**
 * El operador logistico se encarga de la operacion fisica de bodegas y
 * despachos. No tiene atributos propios, solo el rol lo distingue de
 * los demas usuarios.
 */
public class LogisticsOperator extends User {

    public LogisticsOperator(EntityId id, String identityDocument, String fullName, String email) {
        super(id, identityDocument, fullName, email, Role.LOGISTICS_OPERATOR);
    }
}
