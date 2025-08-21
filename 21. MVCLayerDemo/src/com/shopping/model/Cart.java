package com.shopping.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Cart implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId;
	private Map<String, CartItem> items;

	public Cart(String userId) {
		this.userId = userId;
		this.items = new HashMap<>();
	}

	public Map<String, CartItem> getItems() {
		return items;
	}
	
	//카트에 이미 같은 상품이 있는지 확인 후 알맞은 로직 수행 메서드
	public void addProduct(Product product, int quantity) {
		String productId = product.getId();
		
		if (items.containsKey(productId)) {
			CartItem currentItem = items.get(productId);
			currentItem.addQuantity(quantity);
		} else {
			CartItem newItem = new CartItem(product, quantity);
			items.put(productId, newItem);
		}
	}
	
	//카트에서 상품 제거 메서드
	public void removeProduct(String productId) {
	    items.remove(productId);
	}
	
	//카트의 총가격 계산 메서드
	public int getTotalPrice() {
	    int totalPrice = 0;
	    for (CartItem item : items.values()) {
	        totalPrice += item.getTotalPrice();
	    }
	    return totalPrice;
	}
	
	
	//카트 비우기
	public void clear() {
	    items.clear();
	}
	
	@Override
	public String toString() {
	    String result = "==== 장바구니 ====\n";

	    if (items.isEmpty()) {
	        result += "장바구니가 비어있습니다.\n";
	    } else {
	        for (CartItem item : items.values()) {
	            result += item.toString() + "\n";
	        }
	    }

	    result += "=====================\n";
	    result += String.format("총액: %,d원\n", getTotalPrice());
	    
	    return result;
	}

	public String getUserId() {
		return userId;
	}
		
}






















