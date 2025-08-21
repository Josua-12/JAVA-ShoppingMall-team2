package com.shopping.test;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.List;

import com.shopping.Auth.Session;
import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;
import com.shopping.model.Role;
import com.shopping.repository.FileOrderRepository;
import com.shopping.repository.OrderRepository;



@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepo;                 // 영속 계층 (Mock)
    @Mock FileOrderRepository fileOrderRepository;   // 실코드 시그니처상 필요 (Mock)
    @Mock OrderService.ProductRepository productRepo;// 재고 연동 (Mock)

    @Captor ArgumentCaptor<Order> orderCaptor;

    OrderService service;

    // 공용 데이터
    final String ORDER_ID = "O1";
    final String USER = "u1";
    final String OTHER = "other";

    // 간단한 아이템들
    OrderItem i1 = item("P1", "상품1", 1000, 1);
    OrderItem i2 = item("P2", "상품2", 2000, 2); // lineTotal = 4000

    static OrderItem item(String pid, String name, int price, int qty) {
        OrderItem it = new OrderItem(pid, name, price, qty);
        return it;
    }

    static Session adminSession() {
        Session s = new Session();
        s.login("admin", Role.ADMIN);
        return s;
    }

    static Session userSession(String uid) {
        Session s = new Session();
        s.login(uid, Role.USER);
        return s;
    }

    @BeforeEach
    void setUp() {
        service = new OrderService(orderRepo, productRepo, fileOrderRepository);
    }

    Order newPendingOrder(String id, String userId, OrderItem... items) {
        Order o = new Order();           // 기본 PENDING
        o.setOrderId(id);
        o.setUserId(userId);
        for (OrderItem it : items) o.addItem(it);
        return o;
    }

    // -----------------------------
    // place(주문 생성)
    // -----------------------------
    @Nested
    @DisplayName("placeOrder() - 주문 생성")
    class PlaceOrder {

        @Test
        @DisplayName("장바구니/요청이 비어있으면 예외")
        void place_emptyItems_throws() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.placeOrder(USER, List.of(), Role.USER)
            );
            assertTrue(ex.getMessage().contains("items is empty"));
            verify(orderRepo, never()).save(any());
        }

        @Test
        @DisplayName("정상 생성 시 상태=PENDING으로 저장")
        void place_ok_pendingSaved() {
            List<OrderItem> cart = List.of(i1, i2);

            // save() 호출 시점의 객체를 캡쳐해서 상태 확인
            service.placeOrder(USER, cart, Role.USER);

            verify(orderRepo, times(1)).save(orderCaptor.capture());
            Order saved = orderCaptor.getValue();
            assertEquals(OrderStatus.PENDING, saved.getStatus());
            assertEquals(USER, saved.getUserId());
            assertEquals(3, saved.getItems().stream().mapToInt(OrderItem::getQuantity).sum()); // 1 + 2
        }
    }

    // -----------------------------
    // confirm(확정)
    // -----------------------------
    @Nested
    @DisplayName("confirmOrder() - 주문 확정")
    class ConfirmOrder {

        @Test
        @DisplayName("PENDING만 가능 + 재고 충분하면 차감 후 CONFIRMED 저장")
        void confirm_ok_stockSufficient() {
            Order o = newPendingOrder(ORDER_ID, USER, i1, i2);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            // 모든 라인 재고 충분
            when(productRepo.hasStock("P1", 1)).thenReturn(true);
            when(productRepo.hasStock("P2", 2)).thenReturn(true);

            // ADMIN 권한으로 진행(소유권 체킹 우회)
            service.confirmOrder(ORDER_ID, "admin", Role.ADMIN, adminSession());

            // 재고 차감 순서 검증
            InOrder inOrder = inOrder(orderRepo, productRepo);
            inOrder.verify(orderRepo).findById(ORDER_ID);
            inOrder.verify(productRepo).hasStock("P1", 1);
            inOrder.verify(productRepo).hasStock("P2", 2);
            inOrder.verify(productRepo).decreaseStock("P1", 1);
            inOrder.verify(productRepo).decreaseStock("P2", 2);
            inOrder.verify(orderRepo).save(o);

            assertEquals(OrderStatus.CONFIRMED, o.getStatus());
        }

        @Test
        @DisplayName("재고 부족 시 예외(메시지 검증) + 차감/저장 없음")
        void confirm_insufficientStock_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1, i2);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            when(productRepo.hasStock("P1", 1)).thenReturn(true);
            when(productRepo.hasStock("P2", 2)).thenReturn(false); // 부족

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.confirmOrder(ORDER_ID, "admin", Role.ADMIN, adminSession())
            );
            assertTrue(ex.getMessage().contains("재고 부족"));
            assertTrue(ex.getMessage().contains("P2"));

            verify(productRepo, never()).decreaseStock(anyString(), anyInt());
            verify(orderRepo, never()).save(any());
            assertEquals(OrderStatus.PENDING, o.getStatus());
        }

        @Test
        @DisplayName("경계값: 수량=1, 재고=정확히 동일 → 성공")
        void confirm_exactStock_ok() {
            OrderItem only = item("PX", "엣지", 100, 1);
            Order o = newPendingOrder(ORDER_ID, USER, only);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));
            when(productRepo.hasStock("PX", 1)).thenReturn(true);

            service.confirmOrder(ORDER_ID, "admin", Role.ADMIN, adminSession());

            verify(productRepo).decreaseStock("PX", 1);
            verify(orderRepo).save(o);
            assertEquals(OrderStatus.CONFIRMED, o.getStatus());
        }

        @Test
        @DisplayName("PENDING이 아니면 확정 불가")
        void confirm_illegalState_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            o.changeStatus(OrderStatus.CANCELLED);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.confirmOrder(ORDER_ID, "admin", Role.ADMIN, adminSession())
            );
            assertTrue(ex.getMessage().contains("확정할 수 없습니다"));
            verify(orderRepo, never()).save(any());
        }
    }

    // -----------------------------
    // ship / deliver
    // -----------------------------
    @Nested
    @DisplayName("shipOrder(), deliverOrder() - 배송/완료")
    class ShipDeliver {

        @Test
        @DisplayName("CONFIRMED → SHIPPING 가능(ADMIN만)")
        void ship_ok_fromConfirmed() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            o.changeStatus(OrderStatus.CONFIRMED);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            service.shipOrder(ORDER_ID, "admin", Role.ADMIN);

            verify(orderRepo).save(o);
            assertEquals(OrderStatus.SHIPPING, o.getStatus());
        }

        @Test
        @DisplayName("PENDING에서 ship 시도 → 예외")
        void ship_fromPending_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.shipOrder(ORDER_ID, "admin", Role.ADMIN)
            );
            assertTrue(ex.getMessage().contains("배송 시작할 수 없습니다"));
            verify(orderRepo, never()).save(any());
        }

        @Test
        @DisplayName("SHIPPING → DELIVERED 가능(ADMIN만)")
        void deliver_ok_fromShipping() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            o.changeStatus(OrderStatus.CONFIRMED);
            o.changeStatus(OrderStatus.SHIPPING);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            service.deliverOrder(ORDER_ID, "admin", Role.ADMIN);

            verify(orderRepo).save(o);
            assertEquals(OrderStatus.DELIVERED, o.getStatus());
        }
    }

    // -----------------------------
    // cancel
    // -----------------------------
    @Nested
    @DisplayName("cancelOrder() - 취소")
    class CancelOrder {

        @Test
        @DisplayName("사용자 본인은 PENDING에서만 취소 가능")
        void cancel_user_fromPending_ok() {
            Order o = newPendingOrder(ORDER_ID, USER, i1, i2);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            service.cancelOrder(ORDER_ID, USER, Role.USER);

            verify(orderRepo).save(o);
            assertEquals(OrderStatus.CANCELLED, o.getStatus());
            // PENDING → CANCELLED 는 재고 복원 없음
            verify(productRepo, never()).increaseStock(anyString(), anyInt());
        }

        @Test
        @DisplayName("사용자: PENDING 외 상태에서 취소 시 예외")
        void cancel_user_notPending_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            o.changeStatus(OrderStatus.CONFIRMED);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> service.cancelOrder(ORDER_ID, USER, Role.USER)
            );
            assertTrue(ex.getMessage().contains("PENDING 상태에서만"));
            verify(orderRepo, never()).save(any());
        }

        @Test
        @DisplayName("CONFIRMED → CANCELLED 시 재고 복구 호출")
        void cancel_fromConfirmed_restock() {
            Order o = newPendingOrder(ORDER_ID, USER, i1, i2);
            o.changeStatus(OrderStatus.CONFIRMED);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            service.cancelOrder(ORDER_ID, "admin", Role.ADMIN);

            verify(orderRepo).save(o);
            assertEquals(OrderStatus.CANCELLED, o.getStatus());
            verify(productRepo).increaseStock("P1", 1);
            verify(productRepo).increaseStock("P2", 2);
        }

        @Test
        @DisplayName("타인이 사용자 권한으로 취소 시도 → 소유권 위반 예외")
        void cancel_otherUser_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            // USER 권한이면서 소유자 아님 → authorizeOwnership 위반
            assertThrows(IllegalStateException.class,
                () -> service.cancelOrder(ORDER_ID, OTHER, Role.USER));
            verify(orderRepo, never()).save(any());
        }
    }

    // -----------------------------
    // 조회(findById, findByUser) + 권한 필터링
    // -----------------------------
    @Nested
    @DisplayName("조회/권한")
    class QueryAuth {

        @Test
        @DisplayName("getOrder: 본인(USER)만 조회 가능, ADMIN은 모두 가능")
        void getOrder_authz() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            // ADMIN → OK
            Order adminView = service.getOrder(ORDER_ID, "admin", Role.ADMIN, adminSession());
            assertEquals(ORDER_ID, adminView.getOrderId());

            // USER & 본인 → OK
            Order ownerView = service.getOrder(ORDER_ID, USER, Role.USER, userSession(USER));
            assertEquals(ORDER_ID, ownerView.getOrderId());

            // USER & 타인 → 예외
            assertThrows(IllegalStateException.class,
                () -> service.getOrder(ORDER_ID, OTHER, Role.USER, userSession(OTHER)));
        }

        @Test
        @DisplayName("listOrders: USER는 자신의 주문만, ADMIN은 전체")
        void listOrders_filtering() {
            Order a = newPendingOrder("O-a", USER, i1);
            Order b = newPendingOrder("O-b", OTHER, i2);
            when(orderRepo.findAll()).thenReturn(List.of(a, b));
            when(orderRepo.findByUserId(USER)).thenReturn(List.of(a));
            when(orderRepo.findByUserId(OTHER)).thenReturn(List.of(b));

            List<Order> adminList = service.listOrders("admin", Role.ADMIN);
            assertEquals(2, adminList.size());

            List<Order> userList = service.listOrders(USER, Role.USER);
            assertEquals(1, userList.size());
            assertEquals(USER, userList.get(0).getUserId());
        }
    }

    // -----------------------------
    // 가격 고정/아이템 변경 금지(확정 이후)
    // -----------------------------
    @Nested
    @DisplayName("확정 이후 가격/라인 변경 금지")
    class PriceLock {

        @Test
        @DisplayName("CONFIRMED 이후 add/remove/updateItemQty는 예외")
        void afterConfirmed_modifyItems_throws() {
            Order o = newPendingOrder(ORDER_ID, USER, i1);
            o.changeStatus(OrderStatus.CONFIRMED);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));

            // add
            assertThrows(IllegalStateException.class,
                () -> service.addItem(ORDER_ID, item("PX", "추가", 10, 1), USER, Role.USER, userSession(USER)));

            // remove
            assertThrows(IllegalStateException.class,
                () -> service.removeItem(ORDER_ID, "P1", USER, Role.USER, userSession(USER)));

            // update qty
            assertThrows(IllegalStateException.class,
                () -> service.updateItemQty(ORDER_ID, "P1", 99, USER, Role.USER, userSession(USER)));

            // 저장/재고계 호출 없음
            verify(orderRepo, never()).save(any());
            verify(productRepo, never()).decreaseStock(anyString(), anyInt());
            verify(productRepo, never()).increaseStock(anyString(), anyInt());
        }
    }

    // -----------------------------
    // 저장/호출 상호작용: 횟수/순서 verify
    // -----------------------------
    @Nested
    @DisplayName("저장/호출 상호작용 순서 검증")
    class InteractionOrder {

        @Test
        @DisplayName("confirm: findById → hasStock* → decreaseStock* → save 순")
        void confirm_interaction_order() {
            Order o = newPendingOrder(ORDER_ID, USER, i1, i2);
            when(orderRepo.findById(ORDER_ID)).thenReturn(Optional.of(o));
            when(productRepo.hasStock("P1", 1)).thenReturn(true);
            when(productRepo.hasStock("P2", 2)).thenReturn(true);

            service.confirmOrder(ORDER_ID, "admin", Role.ADMIN, adminSession());

            InOrder in = inOrder(orderRepo, productRepo);
            in.verify(orderRepo).findById(ORDER_ID);
            in.verify(productRepo).hasStock("P1", 1);
            in.verify(productRepo).hasStock("P2", 2);
            in.verify(productRepo).decreaseStock("P1", 1);
            in.verify(productRepo).decreaseStock("P2", 2);
            in.verify(orderRepo).save(o);
        }
    }
}
