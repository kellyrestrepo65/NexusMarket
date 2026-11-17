package com.nexusmarket.domain.services.returns;

import com.nexusmarket.domain.exceptions.BusinessRuleException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.enums.ReturnStatus;
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
    public Return execute(EntityId actorId, EntityId orderId, EntityId productId, String reason) {
        User buyer = authorizationService.requireRole(actorId, Role.BUYER);
        Order order = orderLoader.load(orderId);
        if (!order.getBuyerId().equals(buyer.getId())) {
            throw new ForbiddenOperationException("The order " + orderId + " belongs to another buyer");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessRuleException("Only a delivered order can be returned");
        }
        if (productId == null) {
            throw new BusinessRuleException("The product to return is required");
        }
        OrderItem item = order.getItems().stream()
                .filter(candidate -> candidate.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "The product " + productId + " is not part of the order " + orderId));
        boolean alreadyReturned = returnRepositoryPort.findByOrderId(orderId).stream()
                .filter(existing -> existing.getStatus() != ReturnStatus.REJECTED)
                .anyMatch(existing -> existing.isForProduct(productId));
        if (alreadyReturned) {
            throw new BusinessRuleException("The product " + productId + " of the order " + orderId
                    + " already has a return request");
        }
        Product product = orderLoader.product(item);
        return returnRepositoryPort.save(new Return(EntityId.next(), orderId, productId, product.getSellerId(),
                item.getQuantity(), reason));
    }
}
