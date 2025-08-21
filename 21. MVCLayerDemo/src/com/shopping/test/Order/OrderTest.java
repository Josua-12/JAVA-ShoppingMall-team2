package com.shopping.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;

class OrderTest {

    // ====== 헬퍼: 테스트용 OrderItem 생성 ======
    private static OrderItem item(String pid, String name, int price, int qty) {
        return new OrderItem(pid, name, price, qty);
    }

    // ====== 헬퍼: PENDING 상태의 주문 생성 (지정된 items로) ======
    private static Order newPendingOrder(List<OrderItem> items) {
        return new Order(
                "O-TEST-1",
                "u1",
                items,
                LocalDateTime.now(),
                OrderStatus.PENDING
        );
    }

    // 1) 생성/초기값: 상태 PENDING, 주문자/일자 세팅
    @Test
    @DisplayName("생성/초기값: 상태 PENDING, userId/일자 세팅")
    void create_defaultState_pending_ok() {
        List<OrderItem> items = new ArrayList<>();
        Order o = newPendingOrder(items);

        assertAll(
            () -> assertEquals(OrderStatus.PENDING, o.getStatus(), "초기 상태는 PENDING"),
            () -> assertEquals("u1", o.getUserId(), "userId 저장"),
            () -> assertNotNull(o.getOrderDate(), "주문일자 세팅"),
            () -> assertNotNull(o.getItems(), "아이템 리스트 초기화"),
            () -> assertEquals(0, o.getItems().size(), "초기 아이템 0개")
        );
    }

    // 2) 아이템 추가/병합: 같은 productId는 수량 합쳐짐
    @Test
    @DisplayName("아이템 추가/병합: 같은 productId는 수량 합쳐짐")
    void addItem_mergeSameProduct_ok() {
        List<OrderItem> items = new ArrayList<>();
        Order o = newPendingOrder(items);

        o.addItem(item("P1", "Phone", 300_000, 1));
        o.addItem(item("P1", "Phone", 300_000, 2)); // 같은 productId → 수량 병합 기대
        o.addItem(item("P2", "Earbuds", 100_000, 1)); // 다른 productId → 별도 라인

        assertEquals(2, o.getItems().size(), "라인은 2개여야 함");

        OrderItem lineP1 = o.getItems().stream().filter(li -> li.getProductId().equals("P1")).findFirst().orElseThrow();
        assertEquals(3, lineP1.getQuantity(), "P1 수량 1+2=3으로 병합");
    }

    // 3) 아이템 제거/수정 규칙: 없는 productId 제거 시 변화 없음(또는 정책대로)
    @Test
    @DisplayName("아이템 제거: 없는 productId 제거 시 변화 없음(또는 정책에 맞게 동작)")
    void removeItem_nonExisting_noChangeOrPolicy_ok() {
        List<OrderItem> items = new ArrayList<>();
        Order o = newPendingOrder(items);
        o.addItem(item("P1", "Phone", 300_000, 1));

        int before = o.getItems().size();
        o.removeItemByProductId("NOPE"); // 없는 아이템
        assertEquals(before, o.getItems().size(), "변화 없어야 함(정책에 맞게)");
    }

    // 4) 총합 계산: 라인합계 합과 일치
    @Test
    @DisplayName("총합 계산: 라인합계 합과 일치")
    void totalAmount_sumOfLines_ok() {
        List<OrderItem> items = new ArrayList<>();
        Order o = newPendingOrder(items);

        o.addItem(item("P1", "Phone", 300_000, 2));  // 600,000
        o.addItem(item("P2", "Case", 20_000, 3));    // 60,000
        o.addItem(item("P3", "Cable", 10_000, 1));   // 10,000

        long expected = 600_000 + 60_000 + 10_000;
        assertEquals(expected, o.getTotalPrice(), "총합이 라인합계와 일치");
    }

    // 5) 상태 전이: 허용/금지 전이 정확히 강제
    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {
    	
        @Test
        @DisplayName("허용 전이: PENDING → CONFIRMED → SHIPPING → DELIVERED")
        void transition_allowed_ok() {
            Order o = newPendingOrder(new ArrayList<>());
            o.changeStatus(OrderStatus.CONFIRMED);
            assertEquals(OrderStatus.CONFIRMED, o.getStatus());

            o.changeStatus(OrderStatus.SHIPPING);
            assertEquals(OrderStatus.SHIPPING, o.getStatus());

            o.changeStatus(OrderStatus.DELIVERED);
            assertEquals(OrderStatus.DELIVERED, o.getStatus());
        }

        @Test
        @DisplayName("불가 전이: DELIVERED 이후 변경 시 예외")
        void transition_illegal_throws() {
            Order o = newPendingOrder(new ArrayList<>());
            o.changeStatus(OrderStatus.CONFIRMED);
            o.changeStatus(OrderStatus.SHIPPING);
            o.changeStatus(OrderStatus.DELIVERED);

            assertThrows(IllegalStateException.class, () -> o.changeStatus(OrderStatus.CANCELLED),
                    "DELIVERED 이후 취소 불가");
        }
    }

    // 6) 스냅샷: 주문 시점의 가격/이름 유지
    @Test
    @DisplayName("스냅샷: 주문 시점의 상품명/가격 유지")
    void snapshot_keepsPriceAndName_ok() {
        // 1) 주문 생성 시점의 스냅샷
        List<OrderItem> items = new ArrayList<>();
        items.add(item("P1", "Phone", 300_000, 1));
        Order o = newPendingOrder(items);

        // 2) 이후 실제 상품 정보가 바뀌었다고 가정(테스트에선 OrderItem 교체가 아닌, 기존 라인 값이 유지되는지만 확인)
        //   → 도메인 구현이 스냅샷이라면 주문 내부 OrderItem의 name/price는 변하지 않아야 함
        OrderItem line = o.getItems().get(0);
        assertEquals("Phone", line.getProductName());
        assertEquals(300_000, line.getUnitPrice());
    }
}