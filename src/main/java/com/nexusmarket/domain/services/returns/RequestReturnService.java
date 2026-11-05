package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.valueobjects.EntityId;
import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.services.order.OrderLoader;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.ports.in.RequestReturnUseCase;
import org.springframework.stereotype.Service;

/**
 * OBJ-11 "Devoluciones": el comprador solicita la devolucion de una de sus
 * propias ordenes entregadas. Una orden acepta una sola solicitud de devolucion.
 */
@Service
public class RequestReturnService implements RequestReturnUseCase {

    private final AuthorizationService authorizationService;
    private final OrderLoader orderLoader;
    private final ReturnRepositoryPort returnRepositoryPort;

    public RequestReturnService(AuthorizationService authorizationService, OrderLoader orderLoader,
                                ReturnRepositoryPort returnRepositoryPort) {
        this.authorizationService = authorizationService;
        this.orderLoader = orderLoader;
        this.returnRepositoryPort = returnRepositoryPort;
    }

    @Override
    public Return execute(EntityId actorId, EntityId orderId, String reason) {
        User buyer = authorizationService.requireRole(actorId, Role.BUYER);
        Order order = orderLoader.load(orderId);
        if (!order.getBuyerId().equals(buyer.getId())) {
            throw new ForbiddenOperationException("The order " + orderId + " belongs to another buyer");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Only a delivered order can be returned");
        }
        if (returnRepositoryPort.findByOrderId(orderId).isPresent()) {
            throw new BusinessRuleException("The order " + orderId + " already has a return request");
        }
        return returnRepositoryPort.save(new Return(EntityId.next(), orderId, reason));
    }
}
