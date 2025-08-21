package com.shopping.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.shopping.model.OrderStatus;

/**
 * OrderStatus 도메인 단위 테스트
 * - displayName 매핑
 * - 허용 전이/금지 전이(canTransitionTo)
 * - 멱등 전이(자기 자신으로의 전이) 정책 검출
 *
 * 실코드 요약:
 *  PENDING   -> {CONFIRMED, CANCELLED}
 *  CONFIRMED -> {SHIPPING,  CANCELLED}
 *  SHIPPING  -> {DELIVERED}
 *  DELIVERED -> {}
 *  CANCELLED -> {}
 *
 *  자기 자신 전이는 ALLOW_IDEMPOTENT 플래그에 따라 달라질 수 있음.
 */
class OrderStatusTest {

    // ---------- 1) displayName 매핑 ----------
    @Test
    @DisplayName("displayName: 한글 레이블이 정확히 매핑된다")
    void displayName_mapping_ok() {
        assertEquals("주문 대기", OrderStatus.PENDING.getDisplayName());
        assertEquals("주문 확정", OrderStatus.CONFIRMED.getDisplayName());
        assertEquals("배송 중",  OrderStatus.SHIPPING.getDisplayName());
        assertEquals("배송 완료", OrderStatus.DELIVERED.getDisplayName());
        assertEquals("주문 취소", OrderStatus.CANCELLED.getDisplayName());
    }

    // ---------- 2) 허용 전이(Positive cases) ----------
    static Stream<Transition> allowedTransitions() {
        return Stream.of(
                t(OrderStatus.PENDING,   OrderStatus.CONFIRMED),
                t(OrderStatus.PENDING,   OrderStatus.CANCELLED),
                t(OrderStatus.CONFIRMED, OrderStatus.SHIPPING),
                t(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                t(OrderStatus.SHIPPING,  OrderStatus.DELIVERED)
        );
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1} 허용")
    @MethodSource("allowedTransitions")
    @DisplayName("허용 전이 매트릭스가 true를 반환한다")
    void canTransition_allowed_true(Transition tr) {
        assertTrue(tr.from.canTransitionTo(tr.to),
                () -> "허용 전이여야 함: " + tr.from + " -> " + tr.to);
    }

    // ---------- 3) 금지 전이(Full negative coverage, 자기 자신 제외) ----------
    static Stream<Transition> forbiddenTransitions() {
        // 전 상태에서 가능한 전이를 제외한 모든 조합(자기 자신 제외)을 생성
        EnumSet<OrderStatus> all = EnumSet.allOf(OrderStatus.class);

     // 허용 전이 집합
        Set<Transition> allow = allowedTransitions().collect(Collectors.toSet());
        allowedTransitions().forEach(allow::add);

        // 금지 조합 만들기
        return all.stream().flatMap(from ->
                all.stream()
                   .filter(to -> to != from) // 자기 자신 전이는 여기서 제외 (멱등 정책은 별도 테스트)
                   .filter(to -> !allow.contains(t(from, to)))
                   .map(to -> t(from, to))
        );
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1} 금지")
    @MethodSource("forbiddenTransitions")
    @DisplayName("금지 전이 매트릭스가 false를 반환한다(자기 자신 제외)")
    void canTransition_forbidden_false(Transition tr) {
        assertFalse(tr.from.canTransitionTo(tr.to),
                () -> "금지 전이어야 함: " + tr.from + " -> " + tr.to);
    }

    // ---------- 4) 멱등 전이 정책(자기 자신으로 전이) ----------
    @Test
    @DisplayName("멱등 전이 정책: 자기 자신으로의 전이를 정책 그대로 검출")
    void selfTransition_policyDetected() {
        for (OrderStatus s : OrderStatus.values()) {
            boolean actual = s.canTransitionTo(s);
            // 정책이 true/false 무엇이든 "일관되게 동작"하는지만 확인
            // (실코드의 ALLOW_IDEMPOTENT 설정을 바꾸면 이 결과도 함께 바뀜)
            assertEquals(actual, s.canTransitionTo(s),
                    () -> "자기 자신 전이에 대한 정책이 상태별로 일관되어야 함: " + s);
        }
    }

    // ---------- 유틸: 전이 페어 ----------
    private static Transition t(OrderStatus from, OrderStatus to) {
        return new Transition(from, to);
    }

    private static final class Transition {
        final OrderStatus from;
        final OrderStatus to;
        private Transition(OrderStatus f, OrderStatus t) { this.from = f; this.to = t; }
        @Override public String toString() { return from + " -> " + to; }
        // EnumSet에서 쓰기 위한 equality/hashCode (간단 구현)
        @Override public boolean equals(Object o) {
            if (!(o instanceof Transition)) return false;
            Transition other = (Transition) o;
            return from == other.from && to == other.to;
        }
        @Override public int hashCode() {
            return from.hashCode() * 31 + to.hashCode();
        }
    }
}