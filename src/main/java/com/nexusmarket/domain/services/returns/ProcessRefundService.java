package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.models.user.Administrator;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.models.order.Refund;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.ProcessRefundUseCase;
import org.springframework.stereotype.Service;

/**
 * OBJ-11 "Reembolsos": el Administrador procesa o rechaza el reembolso pendiente
 * de una devolucion aprobada.
 */
@Service
public class ProcessRefundService implements ProcessRefundUseCase {

    private final AuthorizationService authorizationService;
    private final ReturnLoader returnLoader;
    private final ReturnRepositoryPort returnRepositoryPort;

    public ProcessRefundService(AuthorizationService authorizationService, ReturnLoader returnLoader,
                                ReturnRepositoryPort returnRepositoryPort) {
        this.authorizationService = authorizationService;
        this.returnLoader = returnLoader;
        this.returnRepositoryPort = returnRepositoryPort;
    }

    @Override
    public Return process(EntityId actorId, EntityId returnId) {
        Return orderReturn = load(actorId, returnId);
        refundOf(orderReturn).process();
        return returnRepositoryPort.save(orderReturn);
    }

    @Override
    public Return reject(EntityId actorId, EntityId returnId) {
        Return orderReturn = load(actorId, returnId);
        refundOf(orderReturn).reject();
        return returnRepositoryPort.save(orderReturn);
    }

    private Return load(EntityId actorId, EntityId returnId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR);
        return returnLoader.load(returnId);
    }

    private Refund refundOf(Return orderReturn) {
        if (orderReturn.getRefund() == null) {
            throw new BusinessRuleException("The return " + orderReturn.getId() + " has no refund");
        }
        return orderReturn.getRefund();
    }
}
