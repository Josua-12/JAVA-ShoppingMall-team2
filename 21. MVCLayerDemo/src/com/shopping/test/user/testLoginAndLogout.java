package com.shopping.test.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.shopping.Auth.Session;
import com.shopping.model.Role;
import com.shopping.model.User;

public class testLoginAndLogout {

	
	@Test
	void testLoginAndLogout() {
	    Session session = new Session();
	    User user = new User("id", "pw", "email", "name");
	    session.login("id", Role.USER, user);
	    assertTrue(session.isLoggedIn());
	    assertEquals(Role.USER, session.getRole());
	    assertEquals(user, session.getUser());

	    session.logout();
	    assertFalse(session.isLoggedIn());
	    assertNull(session.getUserId());
	    assertNull(session.getRole());
	    assertNull(session.getUser());
	}

}
