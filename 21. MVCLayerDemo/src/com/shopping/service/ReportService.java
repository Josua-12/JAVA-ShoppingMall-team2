package com.shopping.service;

import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;
import com.shopping.repository.OrderRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 매출 및 주문 관련 통계를 생성하는 서비스 클래스입니다.
 * 주문 데이터의 조회 및 집계 로직을 담당하여 OrderService의 역할을 분리합니다.
 */
public class ReportService {

    private final OrderRepository orderRepo;

    /**
     * ReportService 생성자
     * @param orderRepo 주문 데이터에 접근하기 위한 리포지토리
     */
    public ReportService(OrderRepository orderRepo) {
        this.orderRepo = Objects.requireNonNull(orderRepo, "OrderRepository는 null일 수 없습니다.");
    }

    /**
     * 지정된 기간 동안의 총 매출을 계산합니다.
     * '주문 확정', '배송 중', '배송 완료' 상태의 주문만 매출로 집계합니다.
     *
     * @param from 시작일
     * @param to   종료일
     * @return 기간 내 총 매출액
     */
    public int salesByDate(LocalDate from, LocalDate to) {
        // 실제 매출로 간주할 수 있는 주문 상태 집합
        EnumSet<OrderStatus> salesStatus = EnumSet.of(
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED
        );

        return orderRepo.findByDateRange(from, to).stream()
                .filter(order -> salesStatus.contains(order.getStatus())) // 매출 집계에 포함될 상태인지 확인
                .mapToInt(Order::getTotalPrice)
                .sum();
    }

    /**
     * 가장 많이 팔린 상위 N개의 상품 목록을 수량 기준으로 조회합니다.
     * 매출 집계와 동일한 상태의 주문들을 대상으로 합니다.
     *
     * @param n 조회할 상품의 개수
     * @return 상품 ID를 key로, 총 판매 수량을 value로 갖는 Map (판매량 내림차순 정렬)
     */
    public Map<String, Integer> topProducts(int n) {
        EnumSet<OrderStatus> salesStatus = EnumSet.of(
            OrderStatus.CONFIRMED,
            OrderStatus.SHIPPING,
            OrderStatus.DELIVERED
        );

        // 1. 모든 주문에서 OrderItem들을 추출하여 하나의 스트림으로 만듭니다.
        Map<String, Integer> productSales = orderRepo.findAll().stream()
                .filter(order -> salesStatus.contains(order.getStatus()))
                .flatMap(order -> order.getItems().stream()) // List<Order> -> Stream<OrderItem>
                // 2. 상품 ID(productId)로 그룹화하고, 각 그룹의 수량(quantity)을 합산합니다.
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(OrderItem::getQuantity)
                ));

        // 3. 합산된 수량을 기준으로 내림차순 정렬하고 상위 n개만 선택합니다.
        return productSales.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new // 순서가 보장되는 LinkedHashMap 사용
                ));
    }

    /**
     * 전체 주문을 각 상태별로 몇 건인지 집계합니다.
     *
     * @return 주문 상태를 key로, 주문 건수를 value로 갖는 Map
     */
    public Map<OrderStatus, Long> orderCountByStatus() {
        return orderRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        Order::getStatus,
                        Collectors.counting() // 각 그룹의 요소 개수를 셉니다.
                ));
    }
}