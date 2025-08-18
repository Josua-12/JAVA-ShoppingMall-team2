package com.shopping.repository;

import com.shopping.model.Order;
import com.shopping.model.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

		// 생성/수정 (동일 ID가 있으면 갱신, 없으면 신규)
		void save(Order order);

		// 단건 조회
		Optional<Order> findById(String orderId);

		// 전체 조회
		List<Order> findAll();
		
		// 조건 조회 (프로젝트에 필요한 것만 우선)
		List<Order> findByUserId(String customerId);
		List<Order> findByStatus(OrderStatus status);
		List<Order> findByDateRange(LocalDate from, LocalDate to);
		
		// 상태만 변경 (편의 메서드)
		boolean updateStatus(String orderId, OrderStatus newStatus);

		// 삭제
		boolean delete(String orderId);
		
//	    // (선택) 페이징
//	    List<Order> findAll(int page, int size);
		
	    // ID 생성기 제공 (파일 구현체에 위임 가능)
	    String nextId();

}
