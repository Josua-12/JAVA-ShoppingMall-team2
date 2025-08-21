package com.shopping.test;

import com.shopping.Auth.Session;
import com.shopping.model.Order;
import com.shopping.model.Role;
import com.shopping.service.OrderService;
import com.shopping.controller.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderController 메뉴 흐름 테스트 (실코드 시그니처에 맞춤)
 * - Scanner 입력을 흉내 내서 메뉴 분기 → 서비스 호출을 검증
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock OrderService service;

    private Session loginUser(String userId) {
        Session s = new Session();
        s.login(userId, Role.USER);
        return s;
    }

    private OrderController newControllerWithInput(OrderService svc, Session session, String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        Scanner sc = new Scanner(in);
        return new OrderController(svc, session, sc);
    }

    @Test
    @DisplayName("orderMenu: 4 선택 → confirmOrder(orderId) 호출")
    void orderMenu_confirm_calls_service() {
        // Given
        Session u1 = loginUser("U1");
        // orderMenu는 먼저 '선택 번호'를 읽고, 4일 때 confirmOrder로 넘어가며 추가로 orderId를 입력받음
        // 입력 시퀀스: [4][개행][O1][개행]
        OrderController c = newControllerWithInput(service, u1, "4\nO1\n");

        doNothing().when(service).confirmOrder(eq("O1"), eq("U1"), eq(Role.USER), same(u1));

        // When
        c.orderMenu();

        // Then
        verify(service, times(1)).confirmOrder("O1", "U1", Role.USER, u1);
    }

    @Test
    @DisplayName("orderHistoryMenu: 1 선택 → listOrders(userId, role) 호출")
    void orderHistoryMenu_list_calls_service() {
        // Given
        Session u1 = loginUser("U1");
        // 입력 시퀀스: [1][개행]
        OrderController c = newControllerWithInput(service, u1, "1\n");

        when(service.listOrders("U1", Role.USER)).thenReturn(List.of());

        // When
        c.orderHistoryMenu();

        // Then
        verify(service, times(1)).listOrders("U1", Role.USER);
    }

    @Test
    @DisplayName("orderHistoryMenu: 2 선택 → getOrder(orderId) 호출")
    void orderHistoryMenu_detail_calls_service() {
        // Given
        Session u1 = loginUser("U1");
        // 입력 시퀀스: [2][개행][O9][개행]
        OrderController c = newControllerWithInput(service, u1, "2\nO9\n");

        when(service.getOrder(eq("O9"), eq("U1"), eq(Role.USER), same(u1)))
            .thenReturn(new Order());

        // When
        c.orderHistoryMenu();

        // Then
        verify(service, times(1)).getOrder("O9", "U1", Role.USER, u1);
    }

    @Test
    @DisplayName("orderHistoryMenu: 3 선택 → cancelOrder(orderId) 호출")
    void orderHistoryMenu_cancel_calls_service() {
        // Given
        Session u1 = loginUser("U1");
        // 입력 시퀀스: [3][개행][O5][개행]
        OrderController c = newControllerWithInput(service, u1, "3\nO5\n");

        doNothing().when(service).cancelOrder("O5", "U1", Role.USER);

        // When
        c.orderHistoryMenu();

        // Then
        verify(service, times(1)).cancelOrder("O5", "U1", Role.USER);
    }

    @Test
    @DisplayName("orderHistoryMenu: 2(상세)에서 반환 값을 화면에 출력하는 흐름(호출 인자 캡처)")
    void orderHistoryMenu_detail_argument_capture() {
        // Given
        Session u1 = loginUser("U1");
        OrderController c = newControllerWithInput(service, u1, "2\nO777\n");

        when(service.getOrder(anyString(), anyString(), any(), any()))
            .thenReturn(new Order());

        // When
        c.orderHistoryMenu();

        // Then - 호출 인자 검사(캡처)
        ArgumentCaptor<String> idCap = ArgumentCaptor.forClass(String.class);
        verify(service).getOrder(idCap.capture(), eq("U1"), eq(Role.USER), same(u1));
        assertEquals("O777", idCap.getValue());
    }
}