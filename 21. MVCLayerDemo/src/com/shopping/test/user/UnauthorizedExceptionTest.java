package com.shopping.test.user;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnauthorizedExceptionTest {  // Test suffix 추천!

    @Test
    void testExceptionMessageAndCause() {
        Exception cause = new Exception("원인");
        com.shopping.exception.UnauthorizedException ex =
            new com.shopping.exception.UnauthorizedException("메시지", cause);
        assertEquals("메시지", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

