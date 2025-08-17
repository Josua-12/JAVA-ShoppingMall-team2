package com.shopping.model;

import java.io.Serializable;

public class CartItem implements Serializable {

	private Product product;
	private int quantity;
	
	public CartItem(Product product, int quantity) {
		//super();
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}
	
	// 상품 가격 getter
	public int getTotalPrice() {
		return product.getPrice()*quantity;
	}
	
	// 수량 추가 메서드
	public void addQuantity(int amount) {
		this.quantity += amount;
	}
	
	// 수량 지정 메서드
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	
	@Override
	public String toString() {
		return String.format("%s - %d개, 총 %,d원", 
				 product.getName(), 
				 quantity, 
				 getTotalPrice());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		
		if(obj == null || getClass() != obj.getClass()) return false;
		
		CartItem otherItem = (CartItem)obj;
		return this.product.equals(otherItem.product);
		
	}
	
	
	
	
	
}
