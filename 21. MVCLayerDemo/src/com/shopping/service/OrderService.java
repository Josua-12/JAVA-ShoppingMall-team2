package com.shopping.service;

import com.shopping.Auth.Session;
import com.shopping.model.*;
import com.shopping.repository.OrderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * OrderService
 * ------------
 * - 비즈니스 규칙(상태 전이/권한/재고)을 담당하는 서비스 계층
 * - Repository는 영속화만, 도메인(Order)은 엔티티 일관성만 담당
 *
 * 상태 정책:
 *  PENDING → CONFIRMED → SHIPPING → DELIVERED
 *     └─────────┐
 *               └→ CANCELLED
 *
 * 권한 정책:
 *  - USER:
 *      · 자신의 주문만 조회/조작 가능
 *      · PENDING 상태에서만 취소 가능
 *      · 확정(CONFIRMED) 동작은 본인 주문에 한해 가능
 *  - ADMIN:
 *      · 모든 주문 조회/조작 가능
 *      · CONFIRMED 이후 단계(배송/완료) 처리 가능
 *      · CONFIRMED 주문도 정책에 따라 취소 가능(재고 복구)
 */
public class OrderService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;


//    public OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
//        this.orderRepo = Objects.requireNonNull(orderRepo);
//        this.productRepo = Objects.requireNonNull(productRepo);
//    }
    
    public OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // =========================
    // 생성/조회
    // =========================
    public Order placeOrder(String userId, List<OrderItem> items, Role actorRole) {
        requireNonBlank(userId, "userId");
        requireNotEmpty(items, "items");

        Order order = new Order(); // PENDING 상태 기본
        order.setUserId(userId);
        for (OrderItem it : items) {
            order.addItem(it);
        }
        orderRepo.save(order);
        return order;
    }

    public Order getOrder(String orderId, String actorUserId, Role actorRole, Session session) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderId));
        authorizeOwnership(order, actorUserId, actorRole);
        return order;
    }

    public List<Order> listOrders(String actorUserId, Role actorRole) {
        if (actorRole == Role.ADMIN) {
            return orderRepo.findAll();
        }
        return orderRepo.findByUserId(actorUserId);
    }

    public List<Order> listOrdersByStatus(OrderStatus status, String actorUserId, Role actorRole) {
        if (actorRole == Role.ADMIN) {
            return orderRepo.findByStatus(status);
        }
        return orderRepo.findByStatus(status).stream()
                .filter(o -> Objects.equals(o.getUserId(), actorUserId))
                .toList();
    }

    public List<Order> listOrdersByDateRange(LocalDate from, LocalDate to, String actorUserId, Role actorRole) {
        if (actorRole == Role.ADMIN) {
            return orderRepo.findByDateRange(from, to);
        }
        return orderRepo.findByDateRange(from, to).stream()
                .filter(o -> Objects.equals(o.getUserId(), actorUserId))
                .toList();
    }

    // =========================
    // 상태 전이
    // =========================
    public void confirmOrder(String orderId, String actorUserId, Role actorRole, Session session) {
        Order order = getOrder(orderId, actorUserId, actorRole, session);

        if (!order.getStatus().canTransitionTo(OrderStatus.CONFIRMED)) {
            throw new IllegalStateException("이 주문은 확정할 수 없습니다. 현재 상태: " + order.getStatus());
        }

        for (OrderItem it : order.getItems()) {
            if (!productRepo.hasStock(it.getProductId(), it.getQuantity())) {
                throw new IllegalStateException("재고 부족: " + it.getProductId());
            }
        }
        for (OrderItem it : order.getItems()) {
            productRepo.decreaseStock(it.getProductId(), it.getQuantity());
        }

        order.changeStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);
    }

    public void shipOrder(String orderId, String actorUserId, Role actorRole) {
        requireAdmin(actorRole);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderId));

        if (!order.getStatus().canTransitionTo(OrderStatus.SHIPPING)) {
            throw new IllegalStateException("이 주문은 배송 시작할 수 없습니다. 현재 상태: " + order.getStatus());
        }

        order.changeStatus(OrderStatus.SHIPPING);
        orderRepo.save(order);
    }

    public void deliverOrder(String orderId, String actorUserId, Role actorRole) {
        requireAdmin(actorRole);
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderId));

        if (!order.getStatus().canTransitionTo(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("이 주문은 배송 완료할 수 없습니다. 현재 상태: " + order.getStatus());
        }

        order.changeStatus(OrderStatus.DELIVERED);
        orderRepo.save(order);
    }

    public void cancelOrder(String orderId, String actorUserId, Role actorRole) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderId));

        if (actorRole == Role.USER) {
            authorizeOwnership(order, actorUserId, actorRole);
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("사용자는 PENDING 상태에서만 취소할 수 있습니다. 현재: " + order.getStatus());
            }
        }

        boolean needRestock = (order.getStatus() == OrderStatus.CONFIRMED);

        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new IllegalStateException("이 주문은 취소할 수 없습니다. 현재 상태: " + order.getStatus());
        }

        order.changeStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);

        if (needRestock) {
            for (OrderItem it : order.getItems()) {
                productRepo.increaseStock(it.getProductId(), it.getQuantity());
            }
        }
    }

    // =========================
    // 아이템 조작
    // =========================
    public void addItem(String orderId, OrderItem item, String actorUserId, Role actorRole, Session session) {
        Order order = getOrder(orderId, actorUserId, actorRole, session);
        order.addItem(item);
        orderRepo.save(order);
    }

    public void removeItem(String orderId, String productId, String actorUserId, Role actorRole, Session session) {
        Order order = getOrder(orderId, actorUserId, actorRole, session);
        order.removeItemByProductId(productId);
        orderRepo.save(order);
    }

    public void updateItemQty(String orderId, String productId, int newQty, String actorUserId, Role actorRole, Session session) {
        Order order = getOrder(orderId, actorUserId, actorRole, session);
        order.updateItemQuantity(productId, newQty);
        orderRepo.save(order);
    }

    // =========================
    // 헬퍼
    // =========================
    private void authorizeOwnership(Order order, String actorUserId, Role role) {
        if (role == Role.ADMIN) return;
        if (!Objects.equals(order.getUserId(), actorUserId)) {
            throw new SecurityException("본인 주문만 접근할 수 있습니다.");
        }
    }

    private void requireAdmin(Role role) {
        if (role != Role.ADMIN) {
            throw new SecurityException("ADMIN 권한이 필요합니다.");
        }
    }

    private static void requireNonBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is blank");
    }

    private static <T> void requireNotEmpty(List<T> list, String field) {
        if (list == null || list.isEmpty()) throw new IllegalArgumentException(field + " is empty");
    }

    // =========================
    // 최소 ProductRepository 인터페이스
    // =========================
    public interface ProductRepository {
        boolean hasStock(String productId, int qty);
        void decreaseStock(String productId, int qty);
        void increaseStock(String productId, int qty);
    }
}
