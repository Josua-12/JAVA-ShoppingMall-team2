package com.shopping.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.shopping.model.OrderItem;

/**
 * OrderItem 도메인 단위 테스트
 * - 생성 유효성(Null/음수/0)
 * - 라인합계(getLineTotal)
 * - 수량 변경 규칙(addQuantity/setQuantity)
 * - 동등성/해시(productId 기준)
 * - toString 포맷("%s(%s) x %d = %,d원")
 *
 * 주의:
 * · 예외 타입이 다르면 IllegalArgumentException → 네 구현의 예외 클래스로 변경해줘.
 * · 생성자 시그니처가 다르면 new OrderItem(...) 부분을 맞춰 수정해줘.
 */
class OrderItemTest {

    // ---- 헬퍼 ----
    private static OrderItem item(String pid, String name, int price, int qty) {
        return new OrderItem(pid, name, price, qty);
    }

    // 1) 생성 유효성
    @Nested
    @DisplayName("생성 유효성")
    class Creation {

        @Test
        @DisplayName("정상 생성")
        void create_valid_ok() {
            OrderItem it = item("P1", "Phone", 300_000, 2);
            assertEquals("P1", it.getProductId());
            assertEquals("Phone", it.getProductName());
            assertEquals(300_000, it.getUnitPrice());
            assertEquals(2, it.getQuantity());
            assertEquals(600_000, it.getLineTotal());
        }

        @Test
        @DisplayName("productId가 null/공백이면 예외")
        void create_withNullOrBlankId_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> item(null, "Phone", 100, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> item("   ", "Phone", 100, 1));
        }

        @Test
        @DisplayName("productName이 null/공백이면 예외")
        void create_withNullOrBlankName_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", null, 100, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", "  ", 100, 1));
        }

        @Test
        @DisplayName("단가가 0 또는 음수면 예외")
        void create_withZeroOrNegativePrice_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", "Phone", 0, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", "Phone", -10, 1));
        }

        @Test
        @DisplayName("수량이 0 또는 음수면 예외")
        void create_withZeroOrNegativeQty_throws() {
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", "Phone", 100, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> item("P1", "Phone", 100, -5));
        }
    }

    // 2) 라인합계
    @Test
    @DisplayName("getLineTotal = unitPrice * quantity (경계값 포함)")
    void lineTotal_basicAndBoundary_ok() {
        assertEquals(100, item("A", "a", 100, 1).getLineTotal());
        assertEquals(600, item("B", "b", 200, 3).getLineTotal());

        // int 경계 근처(오버플로우 방지 범위에서 테스트)
        assertEquals(2_000_000_000,
                item("C", "c", 1_000_000, 2000).getLineTotal());
    }

    // 3) 수량 변경 규칙
    @Nested
    @DisplayName("수량 변경 규칙")
    class QuantityMutation {

        @Test
        @DisplayName("addQuantity: 양수만 허용, 합계 갱신")
        void addQuantity_positive_only_ok() {
            OrderItem it = item("P1", "Phone", 10_000, 1); // 합계 10,000
            it.addQuantity(4); // → 5
            assertEquals(5, it.getQuantity());
            assertEquals(50_000, it.getLineTotal());

            // 0 또는 음수 추가는 예외가 자연스럽다(정책에 맞춰 조정)
            assertThrows(IllegalArgumentException.class, () -> it.addQuantity(0));
            assertThrows(IllegalArgumentException.class, () -> it.addQuantity(-3));
        }

        @Test
        @DisplayName("setQuantity: 1 이상만 허용")
        void setQuantity_minOne_ok() {
            OrderItem it = item("P1", "Phone", 10_000, 2);
            it.setQuantity(3);
            assertEquals(3, it.getQuantity());
            assertEquals(30_000, it.getLineTotal());

            assertThrows(IllegalArgumentException.class, () -> it.setQuantity(0));
            assertThrows(IllegalArgumentException.class, () -> it.setQuantity(-1));
        }
    }

    // 4) equals/hashCode (productId 기준)
    @Test
    @DisplayName("equals/hashCode: productId 기준으로 동등 판단")
    void equals_and_hashCode_byProductId_ok() {
        OrderItem a1 = item("PX", "Phone", 100, 1);
        OrderItem a2 = item("PX", "Phone PRO", 200, 9); // id만 같음
        OrderItem b  = item("PY", "Other", 100, 1);

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());

        assertNotEquals(a1, b);
        assertNotEquals(a1.hashCode(), b.hashCode());

        assertNotEquals(a1, null);
        assertNotEquals(a1, "not-an-item");
    }

    // 5) toString 포맷
    @Test
    @DisplayName("toString: \"{name}({id}) x {qty} = {lineTotal}원\" 형식")
    void toString_format_ok() {
        OrderItem it = item("P1", "갤럭시", 300_000, 2); // 합계 600,000
        String s = it.toString();

        // 실코드의 포맷: String.format("%s(%s) x %d = %,d원", name, id, qty, lineTotal)
        assertTrue(s.contains("갤럭시(P1) x 2 = 600,000원"),
                () -> "실제 toString: " + s);
    }
}
