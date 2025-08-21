package com.shopping.test;

import com.shopping.Auth.Session;
import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;
import com.shopping.model.Role;
import com.shopping.repository.OrderRepository;
import com.shopping.service.OrderService;

import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderService 기능 테스트
 * - 주문 생성/확정/배송/완료/취소
 * - 권한 / 재고 / 아이템 조작
 */
class OrderServiceTest {

    /** ===== 인메모리 OrderRepository(테스트 더블) ===== */
    static class InMemoryOrderRepository implements OrderRepository {
        final Map<String, Order> store = new LinkedHashMap<>();
        long seq = 0;

        @Override public synchronized void save(Order order) {
            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                order.setOrderId(nextId());
            }
            store.put(order.getOrderId(), order);
        }
        @Override public synchronized Optional<Order> findById(String orderId) {
            return Optional.ofNullable(store.get(orderId));
        }
        @Override public synchronized List<Order> findAll() {
            return new ArrayList<>(store.values());
        }
        @Override public synchronized List<Order> findAll(int page, int size) {
            return store.values().stream().skip((long)page*size).limit(size).toList();
        }
        @Override public synchronized boolean delete(String orderId) {
            return store.remove(orderId) != null;
        }
        @Override public synchronized boolean updateStatus(String orderId, OrderStatus newStatus) {
            Order o = store.get(orderId);
            if (o == null) return false;
            o.setStatus(newStatus);
            return true;
        }
        @Override public synchronized List<Order> findByUserId(String userId) {
            return store.values().stream().filter(o -> Objects.equals(o.getUserId(), userId)).toList();
        }
        @Override public synchronized List<Order> findByStatus(OrderStatus status) {
            return store.values().stream().filter(o -> o.getStatus() == status).toList();
        }
        @Override public synchronized List<Order> findByDateRange(java.time.LocalDate from, java.time.LocalDate to) {
            return new ArrayList<>(store.values()); // 간단 구현(실코드에 맞게 필요시 보강)
        }
        @Override public synchronized String nextId() { return "O" + (++seq); }
    }

    /** ===== 스텁 ProductRepository(재고 관리용) ===== */
    static class StubProductRepo implements OrderService.ProductRepository {
        static class P { String id; int stock; P(String id, int stock){this.id=id; this.stock=stock;} }
        final Map<String,P> stock = new ConcurrentHashMap<>();
        StubProductRepo with(String id, int stockQty){ stock.put(id, new P(id, stockQty)); return this; }

        @Override public boolean hasStock(String productId, int qty) {
            P p = stock.get(productId); return p != null && p.stock >= qty;
        }
        @Override public void decreaseStock(String productId, int qty) {
            P p = stock.get(productId);
            if (p == null || p.stock < qty) throw new IllegalStateException("OutOfStock");
            p.stock -= qty;
        }
        @Override public void increaseStock(String productId, int qty) {
            P p = stock.get(productId);
            if (p == null) stock.put(productId, new P(productId, qty));
            else p.stock += qty;
        }
        int stockOf(String productId){ return stock.get(productId).stock; }
    }

    InMemoryOrderRepository repo;
    StubProductRepo productRepo;
    OrderService service;

    Session u1Session, u2Session, adminSession;
    final String U1 = "U1";
    final String U2 = "U2";

    @BeforeEach
    void setUp() {
        repo = new InMemoryOrderRepository();
        productRepo = new StubProductRepo().with("P1", 5).with("P2", 10);
        service = new OrderService(repo, productRepo);

        u1Session = new Session(); u1Session.login(U1, Role.USER);
        u2Session = new Session(); u2Session.login(U2, Role.USER);
        adminSession = new Session(); adminSession.login("ADMIN", Role.ADMIN);
    }

    private OrderItem item(String pid, String pname, int price, int qty) {
        return new OrderItem(pid, pname, price, qty);
    }

    @Test
    @DisplayName("주문 생성: 중복 상품 병합 + 총액 검증")
    void placeOrder_merge_and_total() {
        List<OrderItem> items = new ArrayList<>();
        items.add(item("P1", "상품1", 10_000, 2));
        items.add(item("P1", "상품1", 10_000, 1));
        items.add(item("P2", "상품2", 5_000, 3));

        Order o = service.placeOrder(U1, items, Role.USER);
        assertNotNull(o.getOrderId());
        assertEquals(U1, o.getUserId());
        assertEquals(2, o.getItems().size()); // P1은 병합되어 한 줄
        assertEquals(45_000, o.getTotalPrice());
        assertEquals(OrderStatus.PENDING, o.getStatus());
    }

    @Test
    @DisplayName("확정 → 배송 → 완료: 상태 전이 & 재고 차감")
    void confirm_ship_deliver_with_stock() {
        Order o = service.placeOrder(U1, List.of(
                item("P1","상품1",10_000,3),
                item("P2","상품2",5_000,3)
        ), Role.USER);

        service.confirmOrder(o.getOrderId(), U1, Role.USER, u1Session);
        assertEquals(OrderStatus.CONFIRMED, repo.findById(o.getOrderId()).get().getStatus());
        assertEquals(2, productRepo.stockOf("P1")); // 5 - 3
        assertEquals(7, productRepo.stockOf("P2")); // 10 - 3

        service.shipOrder(o.getOrderId(), U1, Role.USER, u1Session);
        assertEquals(OrderStatus.SHIPPING, repo.findById(o.getOrderId()).get().getStatus());

        service.deliverOrder(o.getOrderId(), U1, Role.USER, u1Session);
        assertEquals(OrderStatus.DELIVERED, repo.findById(o.getOrderId()).get().getStatus());
    }

    @Test
    @DisplayName("취소는 PENDING에서만 가능")
    void cancel_only_pending() {
        Order o1 = service.placeOrder(U1, List.of(item("P1","상품1",10_000,1)), Role.USER);
        assertDoesNotThrow(() -> service.cancelOrder(o1.getOrderId(), U1, Role.USER));
        assertEquals(OrderStatus.CANCELLED, repo.findById(o1.getOrderId()).get().getStatus());

        Order o2 = service.placeOrder(U1, List.of(item("P1","상품1",10_000,1)), Role.USER);
        service.confirmOrder(o2.getOrderId(), U1, Role.USER, u1Session);
        assertThrows(RuntimeException.class, () -> service.cancelOrder(o2.getOrderId(), U1, Role.USER));
    }

    @Test
    @DisplayName("권한: 타인 주문 확정/취소/조회 불가")
    void authorization_checks() {
        Order o = service.placeOrder(U1, List.of(item("P1","상품1",10_000,1)), Role.USER);

        assertThrows(RuntimeException.class, () -> service.confirmOrder(o.getOrderId(), U2, Role.USER, u2Session));
        assertThrows(RuntimeException.class, () -> service.cancelOrder(o.getOrderId(), U2, Role.USER));
        assertThrows(RuntimeException.class, () -> service.getOrder(o.getOrderId(), U2, Role.USER, u2Session));

        // 관리자 정책을 허용한다면 다음을 허용하도록 변경 가능
        // assertDoesNotThrow(() -> service.confirmOrder(o.getOrderId(), "ADMIN", Role.ADMIN, adminSession));
    }

    @Test
    @DisplayName("재고 부족 시 주문/확정 불가")
    void out_of_stock_cases() {
        // 주문 생성 자체에서 수량 검증(정책에 따라 확정 시 검증일 수도)
        assertThrows(RuntimeException.class, () ->
                service.placeOrder(U1, List.of(item("P1","상품1",10_000, 6)), Role.USER));
    }

    @Test
    @DisplayName("아이템 추가/삭제/수량변경 및 총액 반영")
    void add_remove_update_item() {
        Order o = service.placeOrder(U1, List.of(item("P1","상품1",10_000,1)), Role.USER);

        // add
        service.addItem(o.getOrderId(), item("P2","상품2",5_000,3), U1, Role.USER, u1Session);
        Order afterAdd = repo.findById(o.getOrderId()).orElseThrow();
        assertEquals(2, afterAdd.getItems().size());
        assertEquals(25_000, afterAdd.getTotalPrice());

        // update qty
        service.updateItemQty(o.getOrderId(), "P2", 1, U1, Role.USER, u1Session);
        Order afterUpdate = repo.findById(o.getOrderId()).orElseThrow();
        assertEquals(15_000, afterUpdate.getTotalPrice()); // 10,000 + 5,000*1

        // remove
        service.removeItem(o.getOrderId(), "P2", U1, Role.USER, u1Session);
        Order afterRemove = repo.findById(o.getOrderId()).orElseThrow();
        assertEquals(1, afterRemove.getItems().size());
        assertEquals(10_000, afterRemove.getTotalPrice());
    }

    @Test
    @DisplayName("본인 주문 조회 성공")
    void getOrder_ok_for_owner() {
        Order o = service.placeOrder(U1, List.of(item("P1","상품1",10_000,1)), Role.USER);
        Order got = service.getOrder(o.getOrderId(), U1, Role.USER, u1Session);
        assertEquals(o.getOrderId(), got.getOrderId());
        assertEquals(U1, got.getUserId());
    }
}
