package com.shopping.model;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable{
	
	private static final long serialVersionUID = 1L;

	
	private String id;    //고유 식별자
	private String name;  // 상품이름
	private int price;    // 가격
	private int stock;   // 재고
	private String category;  // 카테고리
	
	public Product(String id, String name, int price, int stock, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

	
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

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
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
	
	// equals & hashCode (id 기준)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString (디버그/출력용)
    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%d, stock=%d, category='%s'}",
                id, name, price, stock, category);
    }
}
