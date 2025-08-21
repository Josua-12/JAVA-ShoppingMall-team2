package com.shopping.test.user;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopping.model.User;

public class UserTest {

	
	@Test
	void testDeductBalance_ThrowsWhenInsufficient() {
	    User user = new User("id", "pw", "email", "name");
	    user.setBalance(50);
	    assertThrows(IllegalStateException.class, () -> user.deductBalance(100));
	}
	@Test
	void testToStringFormat() {
	    User user = new User("id1", "password", "email@example.com", "John");
	    user.setBalance(5000);
	    String str = user.toString();
	    assertTrue(str.contains("id1") && str.contains("John") && str.contains("5000"));
	}

}
