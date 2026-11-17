package com.nexusmarket.domain.services.report;

import com.nexusmarket.domain.enums.OrderStatus;
import com.nexusmarket.domain.enums.RefundStatus;
import com.nexusmarket.domain.enums.ReturnStatus;
import com.nexusmarket.domain.enums.Role;
import com.nexusmarket.domain.exceptions.EntityNotFoundException;
import com.nexusmarket.domain.exceptions.ForbiddenOperationException;
import com.nexusmarket.domain.models.catalog.Product;
import com.nexusmarket.domain.models.inventory.Inventory;
import com.nexusmarket.domain.models.order.Order;
import com.nexusmarket.domain.models.order.OrderItem;
import com.nexusmarket.domain.models.order.Return;
import com.nexusmarket.domain.models.report.SellerReport;
import com.nexusmarket.domain.models.report.SellerReport.ProductLine;
import com.nexusmarket.domain.models.user.User;
import com.nexusmarket.domain.ports.in.SellerReportUseCase;
import com.nexusmarket.domain.ports.out.InventoryRepositoryPort;
import com.nexusmarket.domain.ports.out.OrderRepositoryPort;
import com.nexusmarket.domain.ports.out.ProductRepositoryPort;
import com.nexusmarket.domain.ports.out.ReturnRepositoryPort;
import com.nexusmarket.domain.ports.out.UserRepositoryPort;
import com.nexusmarket.domain.services.security.AuthorizationService;
import com.nexusmarket.domain.valueobjects.EntityId;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calcula a partir de lo persistido las ventas, devoluciones, reembolsos e
 * inventario de cada vendedor. Nada se guarda en memoria: todo se recalcula en
 * cada consulta, asi que el reporte es identico antes y despues de reiniciar.
 */
@Service
public class SellerReportService implements SellerReportUseCase {

    private static final Set<OrderStatus> SOLD = Set.of(OrderStatus.PAID, OrderStatus.DISPATCHED,
            OrderStatus.DELIVERED);

    private final AuthorizationService authorizationService;
    private final UserRepositoryPort userRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ReturnRepositoryPort returnRepositoryPort;
    private final InventoryRepositoryPort inventoryRepositoryPort;

    public SellerReportService(AuthorizationService authorizationService, UserRepositoryPort userRepositoryPort,
                               ProductRepositoryPort productRepositoryPort, OrderRepositoryPort orderRepositoryPort,
                               ReturnRepositoryPort returnRepositoryPort,
                               InventoryRepositoryPort inventoryRepositoryPort) {
        this.authorizationService = authorizationService;
        this.userRepositoryPort = userRepositoryPort;
        this.productRepositoryPort = productRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.returnRepositoryPort = returnRepositoryPort;
        this.inventoryRepositoryPort = inventoryRepositoryPort;
    }

    @Override
    public List<SellerReport> all(EntityId actorId) {
        authorizationService.requireRole(actorId, Role.ADMINISTRATOR, Role.SUPERVISOR);
        Data data = load();
        return userRepositoryPort.findAll().stream()
                .filter(user -> user.getRole() == Role.SELLER)
                .sorted(Comparator.comparing(user -> user.getId().value()))
                .map(seller -> build(seller, data))
                .toList();
    }

    @Override
    public SellerReport mine(EntityId actorId) {
        User seller = authorizationService.requireRole(actorId, Role.SELLER);
        return build(seller, load());
    }

    @Override
    public SellerReport of(EntityId actorId, EntityId sellerId) {
        User actor = authorizationService.requireRole(actorId, Role.ADMINISTRATOR, Role.SUPERVISOR, Role.SELLER);
        if (actor.getRole() == Role.SELLER && !actor.getId().equals(sellerId)) {
            throw new ForbiddenOperationException("A seller can only consult its own report");
        }
        User seller = userRepositoryPort.findById(sellerId)
                .filter(user -> user.getRole() == Role.SELLER)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        return build(seller, load());
    }

    @Override
    public List<SellerReport> visibleTo(EntityId actorId) {
        User actor = authorizationService.requireRole(actorId, Role.ADMINISTRATOR, Role.SUPERVISOR, Role.SELLER);
        return actor.getRole() == Role.SELLER ? List.of(build(actor, load())) : all(actorId);
    }

    private record Data(List<Order> orders, Map<EntityId, Order> ordersById, List<Return> returns,
                        List<Inventory> inventories) {
    }

    private Data load() {
        List<Order> orders = orderRepositoryPort.findAll();
        Map<EntityId, Order> byId = orders.stream()
                .collect(Collectors.toMap(Order::getId, Function.identity(), (a, b) -> a));
        return new Data(orders, byId, returnRepositoryPort.findAll(), inventoryRepositoryPort.findAll());
    }

    private SellerReport build(User seller, Data data) {
        List<ProductLine> lines = productRepositoryPort.findBySellerId(seller.getId()).stream()
                .map(product -> line(product, data))
                .sorted(Comparator.comparing(ProductLine::productId))
                .toList();
        BigDecimal gross = sum(lines, ProductLine::sales);
        BigDecimal refunded = sum(lines, ProductLine::refundedAmount);
        return new SellerReport(seller.getId().toString(), seller.getFullName(), lines.size(),
                lines.stream().mapToInt(ProductLine::unitsSold).sum(), gross,
                lines.stream().mapToInt(ProductLine::unitsReturned).sum(), refunded,
                sum(lines, ProductLine::pendingRefundAmount), gross.subtract(refunded),
                lines.stream().mapToInt(ProductLine::availableUnits).sum(),
                lines.stream().mapToInt(ProductLine::reservedUnits).sum(), lines);
    }

    private ProductLine line(Product product, Data data) {
        int sold = 0;
        BigDecimal sales = BigDecimal.ZERO;
        for (Order order : data.orders()) {
            if (SOLD.contains(order.getStatus())) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProductId().equals(product.getId())) {
                        sold += item.getQuantity();
                        sales = sales.add(item.getSubtotal());
                    }
                }
            }
        }
        int returned = 0;
        BigDecimal refunded = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        for (Return r : data.returns()) {
            if (r.getStatus() != ReturnStatus.APPROVED || !r.isForProduct(product.getId())) {
                continue;
            }
            Optional<OrderItem> item = itemOf(r, product.getId(), data);
            if (item.isEmpty()) {
                continue;
            }
            returned += item.get().getQuantity();
            RefundStatus refundStatus = r.getRefund() == null ? null : r.getRefund().getStatus();
            if (refundStatus == RefundStatus.PROCESSED) {
                refunded = refunded.add(item.get().getSubtotal());
            } else if (refundStatus == RefundStatus.PENDING) {
                pending = pending.add(item.get().getSubtotal());
            }
        }
        List<Inventory> stock = data.inventories().stream()
                .filter(inventory -> inventory.getProductId().equals(product.getId())).toList();
        return new ProductLine(product.getId().toString(), product.getName(), product.getType().name(),
                product.getStatus().name(), product.getPrice(), sold, sales, returned, refunded, pending,
                stock.stream().mapToInt(Inventory::getAvailableQuantity).sum(),
                stock.stream().mapToInt(Inventory::getReservedQuantity).sum(),
                stock.stream().mapToInt(Inventory::getDamagedQuantity).sum());
    }

    private static Optional<OrderItem> itemOf(Return r, EntityId productId, Data data) {
        return Optional.ofNullable(data.ordersById().get(r.getOrderId()))
                .flatMap(order -> order.getItems().stream()
                        .filter(item -> item.getProductId().equals(productId)).findFirst());
    }

    private static BigDecimal sum(List<ProductLine> lines, Function<ProductLine, BigDecimal> field) {
        return lines.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
