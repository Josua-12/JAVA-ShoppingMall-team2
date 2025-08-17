package com.shopping.model;

import java.util.EnumMap;
import java.util.EnumSet;

public enum OrderStatus {
    PENDING("주문 대기"),
    CONFIRMED("주문 확정"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String displayName;
    OrderStatus(String dn) { this.displayName = dn; }
    public String getDisplayName() { return displayName; }

    // 전이 허용 테이블
    private static final EnumMap<OrderStatus, EnumSet<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);
    private static final boolean ALLOW_IDEMPOTENT = true; // 동일 상태 허용 여부(정책)

    static {
        ALLOWED.put(PENDING,   EnumSet.of(CONFIRMED, CANCELLED));
        ALLOWED.put(CONFIRMED, EnumSet.of(SHIPPING,  CANCELLED));
        ALLOWED.put(SHIPPING,  EnumSet.of(DELIVERED));
        ALLOWED.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus next) {
        if (next == null) return false;
        if (ALLOW_IDEMPOTENT && this == next) return true;   // 멱등 전이 정책
        return ALLOWED.getOrDefault(this, EnumSet.noneOf(OrderStatus.class)).contains(next);
    }
	

}