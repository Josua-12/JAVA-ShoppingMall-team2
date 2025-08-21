package com.shopping.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 상품 정보를 담는 데이터 모델 클래스 (DTO/VO).
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 2L; // 필드 변경으로 인한 버전 업데이트

    private String id; // 상품 ID (P001 형식, 자동 생성)
    private String name; // 상품명 (100자 이내)
    private ProductCategory category; // 카테고리 (Enum 타입)
    private int price; // 가격 (0 ~ 10,000,000)
    private int stock; // 재고 수량 (0 ~ 9999)
    private String description; // 상품 설명 (500자 이내)
    private LocalDateTime registrationDateTime; // 등록일시 (자동 기록)
    private int salesCount; // 베스트셀러 조회를 위한 판매량

    public Product(String id, String name, ProductCategory category, int price, int stock, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.registrationDateTime = LocalDateTime.now(); // 객체 생성 시 현재 날짜와 시간으로 초기화
        this.salesCount = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getRegistrationDateTime() { return registrationDateTime; }
    public void setRegistrationDateTime(LocalDateTime registrationDateTime) { this.registrationDateTime = registrationDateTime; }
    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
}