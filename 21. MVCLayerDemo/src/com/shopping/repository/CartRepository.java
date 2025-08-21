package com.shopping.repository;

import java.util.Optional;

import com.shopping.model.Cart;

public interface CartRepository {
	 /**
     * 사용자 ID를 이용해 장바구니 정보를 찾습니다.
     * @param userId 사용자 ID
     * @return Optional<Cart> 객체. 장바구니가 없으면 비어있는 Optional을 반환합니다.
     */
    Optional<Cart> findByUserId(String userId);

    /**
     * 장바구니 정보를 저장하거나 업데이트합니다.
     * @param cart 저장할 Cart 객체
     */
    void save(Cart cart);

    /**
     * 특정 사용자의 장바구니를 삭제합니다.
     * @param userId 삭제할 장바구니의 사용자 ID
     */
    void deleteByUserId(String userId);
}
