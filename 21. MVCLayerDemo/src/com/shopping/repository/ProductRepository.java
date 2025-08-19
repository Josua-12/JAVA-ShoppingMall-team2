
package com.shopping.repository;

import java.util.List;
import java.util.Optional;

import com.shopping.model.Product;

public interface ProductRepository {
	
	/**
	 * 상품 데이터 접근을 담당하는 Repository 인터페이스
	 * CRUD + 다양한 조회 조건 제공
	 */
	Product save(Product product); // 상품 저장 (신규 또는 수정)
	
	void saveAll(Product products); // 상품 다중 저장
	
	Optional<Product> findById(String products); // ID로 상품 조회
	
	List<Product> findBynameContains(String name); // 이름(부분 일치)으로 조회
	

    List<Product> findByCategory(String category); // 카테고리로 조회 (옵션)

 
    List<Product> findAll();   // 모든 상품 조회

    
    boolean deleteById(String productId); // 상품 삭제
	

}
