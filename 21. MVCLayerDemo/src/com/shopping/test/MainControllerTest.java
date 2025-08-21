package com.shopping.test;

import com.shopping.Auth.Session;
import com.shopping.model.Role;
import com.shopping.service.OrderService;
import com.shopping.controller.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * MainController 메뉴 라우팅 테스트
 * - Scanner에 가짜 입력을 넣어 메뉴 분기 동작을 재현
 * - OrderController가 주입 가능한 구조라면 mock으로 검증
 * - 주입이 불가능하면 필드 주입(리플렉션)으로 모킹 주입 시도
 *
 * 요구되는 실코드 시그니처(유연하게 대응):
 *   - MainController(OrderService, Session, Scanner [, OrderController?])
 *   - 또는 MainController(OrderService, Session, Scanner) + 내부 필드 orderController
 *   - OrderController에 orderMenu(), orderHistoryMenu()가 존재
 */
@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock OrderService orderService;   // MainController 생성자에 들어갈 서비스(있다면)
    @Mock OrderController orderController; // 라우팅 대상 모킹

    private Session loginUser(String userId) {
        Session s = new Session();
        s.login(userId, Role.USER);
        return s;
    }

    /** 테스트용 입력 Scanner 생성 */
    private Scanner scannerOf(String input) {
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * MainController 인스턴스 생성 헬퍼
     * - 생성자 시그니처가 다를 수 있어 단계적으로 시도
     *   1) (OrderService, Session, Scanner, OrderController)
     *   2) (OrderService, Session, Scanner)
     *      + 필드 주입(orderController)
     */
    private MainController newMain(Session session, Scanner sc, OrderController ocMock) {
        // 1) 4-파라미터 생성자 시도
        try {
            Constructor<MainController> ctor =
                MainController.class.getConstructor(OrderService.class, Session.class, Scanner.class, OrderController.class);
            return ctor.newInstance(orderService, session, sc, ocMock);
        } catch (Throwable ignore) { /* 다음 시도 */ }

        // 2) 3-파라미터 생성자 + 필드 주입
        try {
            Constructor<MainController> ctor =
                MainController.class.getConstructor(OrderService.class, Session.class, Scanner.class);
            MainController mc = ctor.newInstance(orderService, session, sc);
            // 필드 주입 (필드명이 다르면 아래 "orderController"를 실제 필드명으로 바꿔 주세요)
            try {
                Field f = MainController.class.getDeclaredField("orderController");
                f.setAccessible(true);
                f.set(mc, ocMock);
            } catch (NoSuchFieldException noField) {
                // 필드명이 다를 수 있으니 흔한 후보 이름들도 시도
                for (String cand : new String[]{"orderCtl", "orders", "oc"}) {
                    try {
                        Field f2 = MainController.class.getDeclaredField(cand);
                        f2.setAccessible(true);
                        f2.set(mc, ocMock);
                        return mc;
                    } catch (NoSuchFieldException ignore2) { /* continue */ }
                }
                // 그래도 못 찾으면 안내
                throw new IllegalStateException(
                    "MainController 내부에 OrderController 필드가 없거나 이름이 다릅니다. " +
                    "필드명을 'orderController'로 하거나 생성자에 주입받도록 변경해 주세요."
                );
            }
            return mc;
        } catch (Throwable t) {
            throw new IllegalStateException(
                "MainController 생성자 시그니처를 확인하세요. " +
                "지원: (OrderService, Session, Scanner[, OrderController])", t);
        }
    }

    @Test
    @DisplayName("메인메뉴: 주문 메뉴(예: 3) 진입 시 OrderController.orderMenu() 호출")
    void route_to_orderMenu_calls_orderController() {
        Session u1 = loginUser("U1");

        // 입력 시퀀스 예시
        //   3  → 주문 메뉴 진입
        //   0  → (주문 메뉴 종료 후) 메인 종료
        // 네 실코드의 메뉴 숫자가 다르면 3을 해당 번호로 바꿔주세요.
        Scanner sc = scannerOf("3\n0\n");

        MainController mc = newMain(u1, sc, orderController);

        // When
        // 메인 루프를 도는 메서드를 호출 (run(), mainMenu(), start() 등 실코드 이름에 맞추세요)
        callMainLoop(mc);

        // Then
        verify(orderController, atLeastOnce()).orderMenu();
    }

    @Test
    @DisplayName("메인메뉴: 주문 내역/관리(예: 4) 진입 시 OrderController.orderHistoryMenu() 호출")
    void route_to_orderHistory_calls_orderController() {
        Session u1 = loginUser("U1");

        // 입력 시퀀스 예시
        //   4  → 주문 내역/관리 메뉴 진입
        //   0  → 종료
        // 네 실코드의 메뉴 번호에 맞게 4를 조정하세요.
        Scanner sc = scannerOf("4\n0\n");

        MainController mc = newMain(u1, sc, orderController);

        // When
        callMainLoop(mc);

        // Then
        verify(orderController, atLeastOnce()).orderHistoryMenu();
    }

    @Test
    @DisplayName("메인메뉴: 잘못된 번호 입력 후 다시 입력 받아도 예외 없이 동작")
    void invalid_input_then_recover() {
        Session u1 = loginUser("U1");
        // x → 잘못된 입력, 3 → 주문 메뉴, 0 → 종료
        Scanner sc = scannerOf("x\n3\n0\n");

        MainController mc = newMain(u1, sc, orderController);

        assertDoesNotThrow(() -> callMainLoop(mc));
        verify(orderController, atLeastOnce()).orderMenu();
    }

    // ===== 메인 루프 메서드 이름이 프로젝트마다 달라서 유연하게 호출해 주는 헬퍼 =====
    private void callMainLoop(MainController mc) {
        try {
            // 1) run()
            MainController.class.getMethod("run").invoke(mc);
            return;
        } catch (Throwable ignore) { }
        try {
            // 2) mainMenu()
            MainController.class.getMethod("mainMenu").invoke(mc);
            return;
        } catch (Throwable ignore) { }
        try {
            // 3) start()
            MainController.class.getMethod("start").invoke(mc);
            return;
        } catch (Throwable ignore) { }
        throw new IllegalStateException("MainController에 run()/mainMenu()/start() 중 하나가 필요합니다.");
    }
}
