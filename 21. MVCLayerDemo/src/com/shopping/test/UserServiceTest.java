@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    @Test
    void register_success() {
        String username = "alice";
        String email = "alice@example.com";
        String rawPw = "P@ssw0rd!";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPw)).thenReturn("hashed_pw");
        // save 시 id 할당 가정
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("U001");
            return u;
        });

        User result = userService.register(username, email, rawPw);

        assertEquals("U001", result.getId());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.getRoles().contains(Role.USER));
        assertNotEquals(rawPw, result.getPasswordHash());
        assertEquals("hashed_pw", result.getPasswordHash());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));
        assertThrows(DuplicateUsernameException.class,
                () -> userService.register("alice", "a@a.com", "Pw#1abcd"));
    }

    @Test
    void login_success() {
        String username = "alice";
        String rawPw = "P@ssw0rd!";

        User stored = new User();
        stored.setId("U001");
        stored.setUsername(username);
        stored.setStatus(UserStatus.ACTIVE);
        stored.setPasswordHash("hashed_pw");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches(rawPw, "hashed_pw")).thenReturn(true);

        Session session = userService.login(username, rawPw);

        assertEquals("U001", session.getUserId());
        assertNotNull(session.getExpiresAt());
        verify(userRepository).updateLastLogin("U001");
    }

    @Test
    void login_lockedAfterMaxFailures() {
        String username = "alice";
        User stored = new User();
        stored.setId("U001");
        stored.setUsername(username);
        stored.setStatus(UserStatus.ACTIVE);
        stored.setPasswordHash("hashed_pw");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(stored));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        for (int i = 0; i < 5; i++) {
            assertThrows(BadCredentialsException.class,
                    () -> userService.login(username, "wrong" + i));
        }
        // 마지막 실패로 잠금 처리 확인
        verify(userRepository).updateStatus("U001", UserStatus.LOCKED);
    }
}
