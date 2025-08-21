package com.shopping.test.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.shopping.service.AuthService;
import com.shopping.model.User;
import com.shopping.model.Admin;
import com.shopping.model.Role;
import com.shopping.repository.UserRepository;
import com.shopping.repository.AdminRepository;
import com.shopping.util.PasswordEncoder;

public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    AdminRepository adminRepository;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        Mockito.when(userRepository.existsById("user1")).thenReturn(false);
        Mockito.when(userRepository.existsByEmail("email@example.com")).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.registerUser("user1", "password123", "email@example.com", "Name");
        assertEquals("user1", user.getId());
    }

    @Test
    void testRegisterUser_DuplicateIdThrows() {
        Mockito.when(userRepository.existsById("user2")).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> {
            authService.registerUser("user2", "pass1234", "email2@example.com", "Name2");
        });
        assertTrue(ex.getMessage().contains("이미 사용 중인 ID"));
    }

    @Test
    void testLoginSuccessUser() throws Exception {
        User user = new User("user3", PasswordEncoder.hash("pass123"), "email3@example.com", "User3");
        Mockito.when(userRepository.findByEmail("email3@example.com")).thenReturn(user);
        Mockito.when(adminRepository.findByEmail("email3@example.com")).thenReturn(null);

        try (MockedStatic<PasswordEncoder> mocked = Mockito.mockStatic(PasswordEncoder.class)) {
            mocked.when(() -> PasswordEncoder.matches("pass123", user.getPassword())).thenReturn(true);

            Role role = authService.login("email3@example.com", "pass123");
            assertEquals(Role.USER, role);
            assertEquals(user, authService.getLoggedInUser());
        }
    }

    @Test
    void testLoginFailWrongPassword() {
        User user = new User("user4", PasswordEncoder.hash("correctpass"), "email4@example.com", "User4");
        Mockito.when(userRepository.findByEmail("email4@example.com")).thenReturn(user);

        try (MockedStatic<PasswordEncoder> mocked = Mockito.mockStatic(PasswordEncoder.class)) {
            mocked.when(() -> PasswordEncoder.matches("wrongpass", user.getPassword())).thenReturn(false);

            Exception ex = assertThrows(Exception.class, () -> {
                authService.login("email4@example.com", "wrongpass");
            });
            assertTrue(ex.getMessage().contains("이메일 또는 비밀번호가 잘못되었습니다."));
        }
    }

    @Test
    void testLogoutClearsState() throws Exception {
        User user = new User("user5", PasswordEncoder.hash("pwd123"), "email5@example.com", "User5");
        Mockito.when(userRepository.findByEmail("email5@example.com")).thenReturn(user);

        try (MockedStatic<PasswordEncoder> mocked = Mockito.mockStatic(PasswordEncoder.class)) {
            mocked.when(() -> PasswordEncoder.matches("pwd123", user.getPassword())).thenReturn(true);

            authService.login("email5@example.com", "pwd123");
            assertTrue(authService.isLoggedIn());

            authService.logout();
            assertFalse(authService.isLoggedIn());
            assertNull(authService.getLoggedInUser());
        }
    }
}
