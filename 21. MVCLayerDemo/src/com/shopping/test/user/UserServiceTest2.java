package com.shopping.test.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.shopping.service.UserService;
import com.shopping.repository.FileUserRepository;
import com.shopping.model.User;
import com.shopping.util.PasswordEncoder;

public class UserServiceTest2 {

    @Mock
    FileUserRepository mockUserRepo;

    UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(mockUserRepo);
    }

    @Test
    void testRegisterSuccess() throws Exception {
        Mockito.when(mockUserRepo.existsById("id1")).thenReturn(false);
        Mockito.when(mockUserRepo.findByEmail("email@example.com")).thenReturn(null);
        Mockito.when(mockUserRepo.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

        User u = userService.register("id1", "password", "email@example.com", "Name");
        assertEquals("id1", u.getId());
    }

    @Test
    void testRegisterDuplicateIdThrows() {
        Mockito.when(mockUserRepo.existsById("id2")).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> {
            userService.register("id2", "password", "email2@example.com", "Name");
        });
        assertTrue(ex.getMessage().contains("이미 존재하는 ID"));
    }

    @Test
    void testLoginSuccess() throws Exception {
        User user = new User("id3", "hashedpassword", "email3@example.com", "Name3");
        Mockito.when(mockUserRepo.findById("id3")).thenReturn(user);
        try (MockedStatic<PasswordEncoder> mock = Mockito.mockStatic(PasswordEncoder.class)) {
            mock.when(() -> PasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

            User loginUser = userService.login("id3", "password");
            assertEquals("id3", loginUser.getId());
        }
    }

    @Test
    void testLoginFailWrongPassword() {
        User user = new User("id4", "hashedpassword", "email4@example.com", "Name4");
        Mockito.when(mockUserRepo.findById("id4")).thenReturn(user);
        try (MockedStatic<PasswordEncoder> mock = Mockito.mockStatic(PasswordEncoder.class)) {
            mock.when(() -> PasswordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

            Exception ex = assertThrows(Exception.class, () -> userService.login("id4", "wrongpwd"));
            assertTrue(ex.getMessage().contains("비밀번호가 올바르지 않습니다."));
        }
    }

    @Test
    void testUpdatePersonalInfoSuccess() throws Exception {
        User user = new User("id5", "hashedpassword", "email5@example.com", "OldName");
        Mockito.when(mockUserRepo.findById("id5")).thenReturn(user);
        Mockito.when(mockUserRepo.findByEmail("newemail@example.com")).thenReturn(null);
        Mockito.when(mockUserRepo.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updatePersonalInfo("id5", "NewName", "newemail@example.com");
        assertEquals("NewName", user.getName());
        assertEquals("newemail@example.com", user.getEmail());
    }

    @Test
    void testUpdatePasswordSuccess() throws Exception {
        User user = new User("id6", "oldHash", "email6@example.com", "Name6");
        Mockito.when(mockUserRepo.findById("id6")).thenReturn(user);
        Mockito.when(mockUserRepo.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));
        try (MockedStatic<PasswordEncoder> mock = Mockito.mockStatic(PasswordEncoder.class)) {
            mock.when(() -> PasswordEncoder.hash(Mockito.anyString())).thenReturn("newHashedPwd");

            userService.updatePassword("id6", "newPassword");
            assertEquals("newHashedPwd", user.getPassword());
        }
    }
}
