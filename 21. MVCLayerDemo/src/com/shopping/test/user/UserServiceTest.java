package com.shopping.test.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.shopping.service.UserService;
import com.shopping.model.User;
import com.shopping.repository.FileUserRepository;

public class UserServiceTest {
    private UserService userService;
    private FileUserRepository userRepo;

    @BeforeEach
    void setup() {
        userRepo = new FileUserRepository();
        userRepo.deleteAll();  // users.dat 초기화
        userService = new UserService(userRepo);  // 주입 생성자 사용
    }

    @Test
    void testRegisterAndFindById() throws Exception {
        User user = userService.register("test1", "pw1234", "test1@aa.com", "테스터");
        assertNotNull(user);
        assertEquals("test1", user.getId());
        // 아이디로 바로 찾기
        User found = userService.findById("test1");
        assertEquals(user.getEmail(), found.getEmail());
    }

    @Test
    void testDuplicateIdThrows() throws Exception {
        userService.register("test2", "pw", "t2@aa.com", "a");
        Exception ex = assertThrows(Exception.class, () -> {
            userService.register("test2", "pw", "diff@bb.com", "b");
        });
        assertTrue(ex.getMessage().contains("이미 존재하는 ID"));
    }

    @Test
    void testUpdatePersonalInfo() throws Exception {
        userService.register("t4", "pw", "t4@aa.com", "u4");
        userService.updatePersonalInfo("t4", "uu4", "nt4@bb.com");
        User u = userService.findById("t4");
        assertEquals("uu4", u.getName());
        assertEquals("nt4@bb.com", u.getEmail());
    }
}
