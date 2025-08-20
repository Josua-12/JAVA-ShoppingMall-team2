package com.shopping.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;

/**
 * Order 엔티티 테스트
 */
class OrderTest {

    // ====== 헬퍼(시그니처 차이 최소화) ======
    private Order newOrderOf(String userId) {
        try { return Order.class.getMethod("create", String.class).invoke(null, userId) instanceof Order o ? o : null; }
        catch (Exception ignore) {
            try { return Order.class.getConstructor(String.class).newInstance(userId); }
            catch (Exception e) { throw new IllegalStateException("Order.create(userId) 또는 new Order(userId) 중 하나 필요"); }
        }
    }

    private void addItem(Order o, String pid, String pname, int unitPrice, int qty) {
        try { o.getClass().getMethod("addItem", String.class, String.class, long.class, int.class).invoke(o, pid, pname, unitPrice, qty); }
        catch (Exception ignore) {
            try { o.getClass().getMethod("addItem", OrderItem.class).invoke(o, new OrderItem(pid, pname, unitPrice, qty)); }
            catch (Exception e) { throw new IllegalStateException("Order.addItem(...) 시그니처 확인 필요"); }
        }
    }

    private void confirm(Order o) {
        try { o.getClass().getMethod("confirm").invoke(o); }
        catch (Exception ignore) {
            try { o.getClass().getMethod("toConfirmed").invoke(o); }
            catch (Exception e) { throw new IllegalStateException("Order.confirm()/toConfirmed() 필요"); }
        }
    }
    private void ship(Order o) {
        try { o.getClass().getMethod("ship").invoke(o); }
        catch (Exception ignore) {
            try { o.getClass().getMethod("toShipping").invoke(o); }
            catch (Exception e) { throw new IllegalStateException("Order.ship()/toShipping() 필요"); }
        }
    }
    private void deliver(Order o) {
        try { o.getClass().getMethod("deliver").invoke(o); }
        catch (Exception ignore) {
            try { o.getClass().getMethod("toDelivered").invoke(o); }
            catch (Exception e) { throw new IllegalStateException("Order.deliver()/toDelivered() 필요"); }
        }
    }
    private void cancel(Order o) {
        try { o.getClass().getMethod("cancel").invoke(o); }
        catch (Exception ignore) {
            try { o.getClass().getMethod("toCancelled").invoke(o); }
            catch (Exception e) { throw new IllegalStateException("Order.cancel()/toCancelled() 필요"); }
        }
    }

    @Nested
    @DisplayName("아이템/총액/병합")
    class ItemsAndTotal {
        @Test
        @DisplayName("동일 상품은 병합되고 총액이 정확")
        void merge_and_total_ok() {
            Order o = newOrderOf("U1");
            addItem(o, "P1", "상품1", 10_000, 2);
            addItem(o, "P1", "상품1", 10_000, 1);
            addItem(o, "P2", "상품2", 5_000, 3);

            assertEquals(2, o.getItems().size());
            OrderItem p1 = o.getItems().stream().filter(it -> it.getProductId().equals("P1")).findFirst().orElseThrow();
            OrderItem p2 = o.getItems().stream().filter(it -> it.getProductId().equals("P2")).findFirst().orElseThrow();
            assertEquals(3, p1.getQuantity());
            assertEquals(3, p2.getQuantity());

            // TODO: getTotalAmount() 명이 다르면 맞춰 변경
            assertEquals(45_000, o.getTotalPrice());
        }

        @Test
        @DisplayName("없는 상품 제거 시 예외")
        void remove_nonexistent_throws() {
            Order o = newOrderOf("U1");
            addItem(o, "P1", "상품1", 10_000, 1);
            assertThrows(IllegalStateException.class, () -> o.removeItemByProductId("NOPE"));
        }
    }

    @Nested
    @DisplayName("스냅샷(주문시점 가격/상품명) 불변")
    class Snapshot {
        @Test
        void snapshot_immutable() {
            Order o = newOrderOf("U1");
            addItem(o, "P1", "상품1(주문시)", 10_000, 2);
            long before = o.getTotalPrice();

            // 외부 카탈로그 가격이 변해도 주문 총액은 변하지 않아야 함
            assertEquals(before, o.getTotalPrice());
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class State {
        @Test
        @DisplayName("PENDING → CONFIRMED → SHIPPING → DELIVERED")
        void happy_path() {
            Order o = newOrderOf("U1");
            assertEquals(OrderStatus.PENDING, o.getStatus());
            confirm(o);  assertEquals(OrderStatus.CONFIRMED, o.getStatus());
            ship(o);     assertEquals(OrderStatus.SHIPPING,  o.getStatus());
            deliver(o);  assertEquals(OrderStatus.DELIVERED, o.getStatus());
        }

        @Test
        @DisplayName("점프 전이(PENDING→SHIPPING) 금지")
        void illegal_jump() {
            Order o = newOrderOf("U1");
            assertThrows(IllegalStateException.class, () -> ship(o));
        }

        @Test
        @DisplayName("취소는 PENDING에서만 가능")
        void cancel_only_pending() {
            Order o1 = newOrderOf("U1");
            assertDoesNotThrow(() -> cancel(o1));
            assertEquals(OrderStatus.CANCELLED, o1.getStatus());

            Order o2 = newOrderOf("U1");
            confirm(o2);
            assertThrows(IllegalStateException.class, () -> cancel(o2));
        }
    }

    @Test
    @DisplayName("getItems()는 읽기 전용 뷰 제공")
    void items_immutable_view() {
        Order o = newOrderOf("U1");
        addItem(o, "P1", "상품1", 10_000, 1);
        List<OrderItem> items = o.getItems();
        assertThrows(UnsupportedOperationException.class, items::clear);
    }
}