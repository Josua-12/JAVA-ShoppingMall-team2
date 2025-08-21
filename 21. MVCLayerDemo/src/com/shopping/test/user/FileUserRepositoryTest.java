package com.shopping.test.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.io.File;
import com.shopping.model.User;
import com.shopping.repository.FileUserRepository;
import com.shopping.util.Constants;

public class FileUserRepositoryTest {
    private static final String TEST_FILE = "test_users.dat";
    private FileUserRepository repo;

    @BeforeEach
    void setUp() {
        System.setProperty("user.data.file", TEST_FILE);
        repo = new FileUserRepository();
        repo.deleteAll();
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    @Test
    void testSaveNewAndUpdate() {
        User user = new User("id1", "pw", "email@example.com", "Name");
        User saved = repo.save(user);
        assertNotNull(saved);
        assertEquals("id1", saved.getId());

        // Update
        saved.setName("NewName");
        User updated = repo.save(saved);
        assertEquals("NewName", updated.getName());
    }

    @Test
    void testFindByIdAndEmail() {
        User u = new User("id2", "pw", "email2@example.com", "Name2");
        repo.save(u);
        assertEquals("id2", repo.findById("id2").getId());
        assertEquals("email2@example.com", repo.findByEmail("email2@example.com").getEmail());
    }

    @Test
    void testDeleteById() {
        User u = new User("id3", "pw", "email3@example.com", "Name3");
        repo.save(u);
        assertTrue(repo.deleteById("id3"));
        assertNull(repo.findById("id3"));
        assertFalse(repo.deleteById("nonExist"));
    }

    @Test
    void testDuplicateEmailThrows() {
        User u1 = new User("id4", "pw", "dup@example.com", "Name1");
        repo.save(u1);
        User u2 = new User("id5", "pw", "dup@example.com", "Name2");
        assertThrows(IllegalArgumentException.class, () -> repo.save(u2));
    }

    @Test
    void testDeleteAll() {
        repo.save(new User("id6", "pw", "email6@example.com", "Name6"));
        repo.deleteAll();
        assertEquals(0, repo.count());
        assertTrue(repo.findAll().isEmpty());
    }
}
