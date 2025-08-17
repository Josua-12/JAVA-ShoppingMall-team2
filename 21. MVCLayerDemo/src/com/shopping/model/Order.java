package com.shopping.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    // 1. 필드
    private String orderId;   // null 허용: 저장 시 Repository가 발급
    private String userId;
    private List<OrderItem> items;
    private int totalPrice;
    private LocalDateTime orderDate;
    private OrderStatus status;

    // 2-1. 생성자
    public Order() {							// 기본 생성자 → "장바구니에서 주문을 시작" (아직 번호 없음, 기본값 세팅). 객체를 만들자마자 필드를 원하는 값으로 초기화. 
        this.items = new ArrayList<>();			// items를 빈 ArrayList로 만들고,
        this.orderDate = LocalDateTime.now();   // orderDate를 현재 시간으로,
        this.status = OrderStatus.PENDING;     // status를 OrderStatus.PENDING으로 세팅.
        this.totalPrice = 0;
    }

    // 2-2. 오버로딩된 생성자
    public Order(String orderId, String userId, List<OrderItem> items,           // 데이터가 이미 확정된 상태에서 한 번에 만들 때 → 이 경우 매개변수 생성자로 한 번에 세팅 가능
                 LocalDateTime orderDate, OrderStatus status) {                 // 파라미터 생성자 → "이미 확정된 주문을 재구성" (DB에서 꺼내거나 API 응답으로 받아옴).
    	// orderId는 null/blank 허용 → 저장 시 repo가 nextId()로 부여
    	//if (orderId == null || orderId.isBlank()) throw new IllegalArgumentException("orderId empty");    //null 값이나 빈 값 방어 로직 포함 → 잘못된 데이터 방지
        if (userId == null || userId.isBlank())   throw new IllegalArgumentException("userId empty");
        //this.orderId = orderId;
        this.orderId = (orderId != null && !orderId.isBlank()) ? orderId : null;
        this.userId = userId;
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());  // 방어적 복사
        this.orderDate = (orderDate != null) ? orderDate : LocalDateTime.now();   // null-safe 기본값 : null 입력을 방지하기 위해 쓰는 것
        this.status = (status != null) ? status : OrderStatus.PENDING;           // null-safe 기본값
        recalcTotal();
    }

    // 3. 편의 메서드
    // ===== 내부 보조 =====
    /** PENDING에서만 아이템 변경 허용 */
    private void requireModifiable() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("아이템 수정은 PENDING 상태에서만 가능합니다. 현재: " + this.status);
        }
    }

    /** 합계 재계산: 아이템 전수 합 */
    private void recalcTotal() {
        int sum = 0;
        for (OrderItem i : items) sum += i.getLineTotal();
        this.totalPrice = sum;
    }

    // ===== 아이템 조작 (PENDING 전용) =====
    /** 같은 productId는 합산해서 추가 */
    public void addItem(OrderItem item) {
        requireModifiable();
        if (item == null) return;

        for (OrderItem it : items) {
            if (Objects.equals(it.getProductId(), item.getProductId())) {
                it.addQuantity(item.getQuantity());   // 수량 합산
                recalcTotal();
                return;
            }
        }
        this.items.add(item);                          // 신규 라인 추가
        this.totalPrice += item.getLineTotal();        // 미세 최적화(또는 recalcTotal())
    }

    /** productId 기준 제거 */
    public boolean removeItemByProductId(String productId) {
        requireModifiable();
        boolean removed = this.items.removeIf(i -> Objects.equals(i.getProductId(), productId));
        if (removed) recalcTotal();
        return removed;    // 제거 성공 여부(true/false)를 반환
    }

    /** 수량 변경(0 이하이면 제거) */
    public void updateItemQuantity(String productId, int newQty) {
        requireModifiable();
        if (newQty <= 0) { removeItemByProductId(productId); return; }  // 수량이 0 이하로 들어온 경우, 상품을 아예 주문에서 제거
        for (OrderItem it : items) {
            if (Objects.equals(it.getProductId(), productId)) {
                it.setQuantity(newQty);
                recalcTotal();
                return;  // 메서드 종료
            }
        }
        throw new IllegalArgumentException("해당 상품이 주문에 없습니다: " + productId);
    }

    // ===== 상태 변경 =====
    /** 상태 전이: 가능 여부는 enum이 판단 */
      // OrderStatus1에 맞는 버전
//    public void changeStatus(OrderStatus next) {
//        if (next == null) throw new IllegalArgumentException("status null");
//        if (!this.status.canTransitionTo(next)) {
//            throw new IllegalStateException("Invalid status transition: " + this.status + " -> " + next);
//        }
//        this.status = next;
//    }
    
    /**
     * 상태 전이
     * - 전이 가능 여부는 OrderStatus.canTransitionTo(next)가 판단 (전이 테이블/정책 반영)
     * - 멱등 전이(현재 == next)가 "정책상 허용"인 경우 아무 일도 하지 않고 반환
     * - 허용되지 않는 전이는 명확한 메시지로 예외를 던짐
     */
    public void changeStatus(OrderStatus next) {
        if (next == null) throw new IllegalArgumentException("status null");

        // 멱등 전이 처리: 정책상 허용이면 no-op
        if (this.status == next) {
            if (this.status.canTransitionTo(next)) {
                return; // 동일 상태로의 전이를 허용하는 정책(예: ALLOW_IDEMPOTENT=true)일 때는 조용히 통과
            } else {
                // 동일 상태 전이가 금지된 정책이라면 예외
                throw new IllegalStateException("Idempotent transition is not allowed: " + this.status + " -> " + next);
            }
        }

        // 일반 전이 가능 여부 체크
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException("Invalid status transition: " + this.status + " -> " + next);
        }

        // 전이 수행
        this.status = next;

        // (선택) 전이 로그/이벤트 훅이 필요하면 여기서 발행
        // publishDomainEvent(new OrderStatusChangedEvent(orderId, previous, next, LocalDateTime.now()));
    }


    // 4) Getter/Setter (items는 방어적 복사/읽기전용)
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    //public void setUserId(String userId) { this.userId = userId; }
    public void setUserId(String userId) {
    	if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId empty");
    	this.userId = userId;
    	}
    
    
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items != null ? items : new ArrayList<>());
        recalcTotal();
    }

    public int getTotalPrice() { return totalPrice; }  // setTotalPrice는 외부 금지(계산으로만)
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }

    // 5. equals & hashCode : orderId 기준 (null 안전)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }
    @Override 
    public int hashCode() { return Objects.hash(orderId); }

    // 6. toString
    @Override
//    public String toString() {
//        return String.format("Order[id=%s, user=%s, total=%,d, date=%s, status=%s]",
//                orderId, userId, totalPrice, orderDate, status.getDisplayName());
//    }
    public String toString() {
    	String st = (status != null) ? status.getDisplayName() : "null";


    	return String.format("Order[id=%s, user=%s, total=%,d, date=%s, status=%s]",
    	orderId, userId, totalPrice, orderDate, st);

    	}
}