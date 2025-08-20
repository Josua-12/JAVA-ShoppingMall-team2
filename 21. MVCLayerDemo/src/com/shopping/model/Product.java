package com.shopping.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 상품 정보를 담는 모델 클래스(VO/DTO).
 * <p>
 * 설계 요구사항:
 * 1. 필수 필드: id, name, price, stock, category
 * 2. equals()와 hashCode()는 고유 식별자인 id를 기준으로 재정의
 * 3. 파일 직렬화를 통한 데이터 저장을 위해 Serializable 인터페이스 구현
 * </p>
 */
public class Product implements Serializable {

    /**
     * Serializable 인터페이스를 구현하는 클래스의 버전 관리를 위한 ID.
     * 클래스 구조가 변경될 때 이전 버전과의 호환성을 검사하는 데 사용됩니다.
     */
    private static final long serialVersionUID = 1L;

    // --- 필드 ---
    private String id;          // 상품 고유 ID
    private String name;     // 상품 이름
    private double price;    // 상품 가격
    private int stock;       // 재고 수량
    private String category; // 상품 카테고리

    // --- 생성자 ---
    public Product(String id, String name, double price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }


	// --- Getter 및 Setter 메소드 ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // --- 객체 동일성 비교 (id 기준) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- 객체 정보 출력을 위한 toString() 재정의 ---
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                '}';
    }
}