package com.shopping.test.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import com.shopping.service.AuthorizationService;
import com.shopping.service.AuthService;
import com.shopping.exception.UnauthorizedException;
import com.shopping.model.Role;

public class AuthorizationServiceTest {

    private AuthorizationService authorizationService;
    
    @Mock
    private AuthService mockAuthService;

    @BeforeEach
    void setUp() {
    	MockitoAnnotations.openMocks(this);
        authorizationService = new AuthorizationService(mockAuthService);
    }

    @Test
    void testAssertLoggedIn_SucceedsWhenLoggedIn() throws UnauthorizedException {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(true);
        authorizationService.assertLoggedIn();
    }

    @Test
    void testAssertLoggedIn_ThrowsWhenNotLoggedIn() {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> authorizationService.assertLoggedIn());
    }

    @Test
    void testAssertLoggedInWithRole_SucceedsWhenCorrectRole() throws UnauthorizedException {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(true);
        Mockito.when(mockAuthService.getCurrentUserRole()).thenReturn(Role.ADMIN);

        authorizationService.assertLoggedIn(Role.ADMIN);
    }

    @Test
    void testAssertLoggedInWithRole_ThrowsWhenWrongRole() {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(true);
        Mockito.when(mockAuthService.getCurrentUserRole()).thenReturn(Role.USER);

        assertThrows(UnauthorizedException.class, () -> authorizationService.assertLoggedIn(Role.ADMIN));
    }

    @Test
    void testHasRole_ReturnsTrueWhenHasRole() {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(true);
        Mockito.when(mockAuthService.getCurrentUserRole()).thenReturn(Role.USER);

        assertTrue(authorizationService.hasRole(Role.USER));
    }

    @Test
    void testHasRole_ReturnsFalseWhenNoRole() {
        Mockito.when(mockAuthService.isLoggedIn()).thenReturn(true);
        Mockito.when(mockAuthService.getCurrentUserRole()).thenReturn(Role.ADMIN);

        assertFalse(authorizationService.hasRole(Role.USER));
    }
}
