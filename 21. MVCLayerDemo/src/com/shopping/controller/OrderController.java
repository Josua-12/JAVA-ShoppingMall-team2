package com.shopping.controller;

import com.shopping.Auth.Session;
import com.shopping.model.*;
import com.shopping.service.OrderService;

import java.util.List;
import java.util.Scanner;

public class OrderController {

    private final OrderService orderService;
    private final Session session;
    private final Scanner sc;

    public OrderController(OrderService orderService, Session session, Scanner sc) {
        this.orderService = orderService;
        this.session = session;
        this.sc = sc;
    }

    // 주문 메뉴
    public void orderMenu() {
        System.out.println("2.4.1 주문 생성");
        System.out.println("1. 장바구니 상품 전체 주문");
        System.out.println("2. 개별 상품 즉시 구매");
        System.out.println("3. 배송 주소 입력 (기본값: 회원 주소)");
        System.out.println("4. 주문 확인 및 최종 결재");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> placeOrderFromCart();
            case 2 -> placeOrderSingle();
            case 3 -> inputAddress();
            case 4 -> confirmOrder();
            default -> System.out.println("잘못된 선택입니다.");
        }
    }

    // 주문 내역 메뉴
    public void orderHistoryMenu() {
        System.out.println("2.4.2 주문 관리");
        System.out.println("1. 주문 내역 조회");
        System.out.println("2. 주문 상세 정보 조회");
        System.out.println("3. 주문 취소 (PENDING 상태만)");

        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            case 1 -> listOrders();
            case 2 -> getOrderDetail();
            case 3 -> cancelOrder();
            default -> System.out.println("잘못된 선택입니다.");
        }
    }

    private void placeOrderFromCart() {
        System.out.println("장바구니 상품 전체 주문 기능 (구현 예정)");
    }

    private void placeOrderSingle() {
        System.out.println("개별 상품 즉시 구매 기능 (구현 예정)");
    }

    private void inputAddress() {
        System.out.println("배송 주소 입력 (현재는 기본값: 회원 주소)");
    }

    private void confirmOrder() {
        System.out.print("주문 ID 입력: ");
        String orderId = sc.nextLine();

        try {
            orderService.confirmOrder(orderId, session.getUserId(), session.getRole(), session );
            System.out.println("주문이 확정되었습니다.");
        } catch (Exception e) {
            System.out.println("에러: " + e.getMessage());
        }
    }

    private void listOrders() {
        List<Order> orders = orderService.listOrders(session.getUserId(), session.getRole());
        orders.forEach(System.out::println);
    }

    private void getOrderDetail() {
        System.out.print("조회할 주문 ID 입력: ");
        String orderId = sc.nextLine();

        try {
            Order order = orderService.getOrder(orderId, session.getUserId(), session.getRole(), session);
            System.out.println(order);
        } catch (Exception e) {
            System.out.println("에러: " + e.getMessage());
        }
    }

    private void cancelOrder() {
        System.out.print("취소할 주문 ID 입력: ");
        String orderId = sc.nextLine();

        try {
            orderService.cancelOrder(orderId, session.getUserId(), session.getRole());
            System.out.println("주문이 취소되었습니다.");
        } catch (Exception e) {
            System.out.println("에러: " + e.getMessage());
        }
    }
}