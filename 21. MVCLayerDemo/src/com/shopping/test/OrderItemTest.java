package com.shopping.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.shopping.model.OrderItem;

/**
 * OrderItem 단위 테스트
 */
class OrderItemTest {

    @Test
    @DisplayName("라인합계: price * qty")
    void lineTotal_ok() {
        // 가정: new OrderItem(productId, productName, unitPrice, quantity)
        OrderItem i = new OrderItem("P1", "상품1", 10_000, 3);
        // TODO: getLineTotal() 명이 다르면 getTotalPrice() 등으로 변경
        assertEquals(30_000, i.getLineTotal());
    }

    @Nested
    @DisplayName("유효성(수량/가격)")
    class Validation {
        @Test
        void qty_must_be_positive() {
            assertThrows(IllegalArgumentException.class, () -> new OrderItem("P1","상품1",10_000,0));
            assertThrows(IllegalArgumentException.class, () -> new OrderItem("P1","상품1",10_000,-1));
        }
        @Test
        void price_must_be_positive() {
            assertThrows(IllegalArgumentException.class, () -> new OrderItem("P1","상품1",0,1));
            assertThrows(IllegalArgumentException.class, () -> new OrderItem("P1","상품1",-1,1));
        }
    }

    @Test
    @DisplayName("동등성: productId 기준 equals/hashCode(선택)")
    void equality_on_productId() {
        OrderItem a = new OrderItem("P1", "A", 10_000, 1);
        OrderItem b = new OrderItem("P1", "B", 12_000, 2);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
