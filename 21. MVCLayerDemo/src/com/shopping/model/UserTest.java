package com.shopping.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void create_user_success() {
        User u = new User(
                /* id */ "U001",
                /* username */ "alice",
                /* email */ "alice@example.com",
                /* passwordHash */ "hashed",
                /* roles */ Set.of(Role.USER),
                /* status */ UserStatus.ACTIVE,
                /* createdAt */ LocalDateTime.now(),
                /* updatedAt */ LocalDateTime.now()
        );

        assertEquals("alice", u.getUsername());
        assertEquals("alice@example.com", u.getEmail());
        assertTrue(u.getRoles().contains(Role.USER));
        assertEquals(UserStatus.ACTIVE, u.getStatus());
    }

    @Test
    void equals_hashCode_byId() {
        LocalDateTime now = LocalDateTime.now();

        User a = new User("U001", "alice", "a@a.com", "h", Set.of(Role.USER), UserStatus.ACTIVE, now, now);
        User b = new User("U001", "alice2", "b@b.com", "h2", Set.of(Role.ADMIN), UserStatus.LOCKED, now, now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_shouldNotContainPassword() {
        User u = new User("U001", "alice", "a@a.com", "SECRET_HASH", Set.of(Role.USER), UserStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now());

        String s = u.toString();
        assertFalse(s.contains("SECRET_HASH"));
        assertTrue(s.contains("alice"));
    }
}
