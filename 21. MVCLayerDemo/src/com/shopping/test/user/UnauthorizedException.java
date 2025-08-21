package com.shopping.test.user;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;

public class UnauthorizedException {

	@Test
	void testExceptionMessageAndCause() {
	    Exception cause = new Exception("원인");
	    UnauthorizedException ex = new UnauthorizedException("메시지", cause);
	    assertEquals("메시지", ex.getMessage());
	    assertEquals(cause, ex.getCause());
	}

}
