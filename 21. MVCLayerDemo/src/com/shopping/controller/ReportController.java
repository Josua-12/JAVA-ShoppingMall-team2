package com.shopping.controller;

import java.time.LocalDate;
import java.util.Map;

import com.shopping.model.OrderStatus;
import com.shopping.service.ReportService;

public class ReportController {
	
	/*
	 * ReportController
	 * - ReportService에서 생성된 통계 데이터를 사용자에게 보여주는 콘솔 컨트롤러
	 */
	
	private ReportService reportService;
	
	
	public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
	
	
    /*
     * 특정 기간 동안의 매출을 출력합니다.
     */
	 public void printSalesByDate(LocalDate from, LocalDate to) {
	        try {
	            int sales = reportService.salesByDate(from, to);
	            System.out.printf("[매출 통계] %s ~ %s : 총 매출 = %,d원%n",
	                    from, to, sales);
	        } catch (IllegalArgumentException e) {
	            System.out.println("⚠️ 날짜 입력 오류: " + e.getMessage());
	        }
	    }
	 
	 
	 
	    /*
	     * 가장 많이 팔린 상위 N개 상품을 출력합니다.
	     */
	 
	    public void printTopProducts(int n) {
	        try {
	            Map<String, Integer> topProducts = reportService.topProducts(n);
	            System.out.println("[인기 상품 TOP " + n + "]");
	            if (topProducts.isEmpty()) {
	                System.out.println("  판매된 상품이 없습니다.");
	                return;
	            }
	            topProducts.forEach((productId, qty) ->
	                    System.out.printf("  상품ID: %s, 판매수량: %d%n", productId, qty));
	        } catch (IllegalArgumentException e) {
	            System.out.println("⚠️ 잘못된 요청: " + e.getMessage());
	        }
	    }
	    
	    
	    /*
	     * 주문 상태별 건수를 출력합니다.
	     */
	    public void printOrderCountByStatus() {
	        Map<OrderStatus, Long> counts = reportService.orderCountByStatus();
	        System.out.println("[주문 상태별 건수]");
	        for (OrderStatus status : OrderStatus.values()) {
	            long count = counts.getOrDefault(status, 0L);
	            System.out.printf("  %-10s : %d건%n", status, count);
	        }
	    }
	
	
	
}
