package com.shopping.test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.shopping.model.OrderStatus;

/**
 * OrderStatus 열거형 값 검증 + 전이 가능 여부를 엔티티 레벨에서 강제한다면,
 * OrderTest에서 전이 메서드(confirm/ship/deliver/cancel)로 함께 검증함.
 */
class OrderStatusTest {

    @Test
    @DisplayName("열거형 기본값 존재 확인")
    void enum_values_exist() {
        // 필수 상태가 모두 정의되어 있는지 확인
        assertNotNull(OrderStatus.valueOf("PENDING"));
        assertNotNull(OrderStatus.valueOf("CONFIRMED"));
        assertNotNull(OrderStatus.valueOf("SHIPPING"));
        assertNotNull(OrderStatus.valueOf("DELIVERED"));
        assertNotNull(OrderStatus.valueOf("CANCELLED"));
    }
}
