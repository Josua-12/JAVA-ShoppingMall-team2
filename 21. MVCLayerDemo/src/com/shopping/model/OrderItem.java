package com.shopping.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 주문 한 건의 라인아이템(주문 시점 스냅샷)
 * - productId: 식별자(동등성 기준)
 * - productName: 주문 시점의 표시명 (이후 상품명 바뀌어도 영향 없음)
 * - unitPrice: 주문 시점의 단가(원)
 * - quantity: 수량(>=1)
 */
public class OrderItem implements Serializable {
	
	// 1. 필드
    private static final long serialVersionUID = 1L;

    private final String productId;   // 고유 식별 (equals/hashCode 기준)
    private String productName; // 표시용(주문 시점명)
    private int unitPrice;      // 단가(>=0)
    private int quantity;             // 수량(>=1)

    // 2. 생성자
    // 기본 생성자는 직렬화/프레임워크용으로만 필요하면 유지
    public OrderItem() {
        this.productId = "UNKNOWN";
        this.productName = "UNKNOWN";
        this.unitPrice = 0;
        this.quantity = 1;
    }

    // 2-2. 파라미터 생성자
    public OrderItem(String productId, String productName, int unitPrice, int quantity) {    // 파라미터 생성자 → "이미 확정된 주문을 재구성" (DB에서 꺼내거나 API 응답으로 받아옴).
        if (productId == null || productId.isBlank()) throw new IllegalArgumentException("productId empty");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("productName empty");
        if (unitPrice <= 0) throw new IllegalArgumentException("unitPrice must be > 0");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // 4. 편의 메서드
    /** 소계 = 단가 * 수량 */
    public int getLineTotal() {
        return unitPrice * quantity;
    }

    /** 수량 증가 (장바구니→주문 이전 합산 로직에도 유용) */
    public void addQuantity(int delta) {
        if (delta <= 0) throw new IllegalArgumentException("delta <= 0");
        this.quantity += delta;
    }

    // 3. Getter/Setter
    public String getProductId() { return productId; }   // 값 꺼내오는 용도
    public String getProductName() { return productName; }
    public int getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public void setProductName(String productName) {
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("productName empty");
        this.productName = productName;
    }

    /** 정책상 필요할 때만 단가 변경 허용(보통 주문 확정 후엔 변경 안 함) */
    public void setUnitPrice(int unitPrice) {
        if (unitPrice <= 0) throw new IllegalArgumentException("unitPrice <= 0");
        this.unitPrice = unitPrice;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity <= 0");
        this.quantity = quantity;
    }

    // ===== 동등성: productId 기준 =====
    // equals & hashCode를 productId만 같으면 같은 상품으로 본다는 의미로 구현
    
  // 5. equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem that = (OrderItem) o;
        return Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
//
    // 6. toString
    @Override
    public String toString() {
        return String.format("%s(%s) x %d = %,d원", productName, productId, quantity, getLineTotal());
    }

    // ===== 팩토리(선택): Product 스냅샷으로부터 생성 =====
    // Product라는 별도의 상품 모델에서 OrderItem을 바로 만들 수 있는 편의 메서드
    // Product 정보를 복사해와서 OrderItem 생성
    // 주문 시점의 "스냅샷"을 저장하는 의미 (상품 정보가 이후 바뀌어도 주문 내역은 그대로 유지됨)
    
    public static OrderItem fromProduct(Product p, int quantity) {
        return new OrderItem(p.getId(), p.getName(), p.getPrice(), quantity);
    }
    
    
}
