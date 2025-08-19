package com.shopping.repository;

import java.util.List;
import com.shopping.model.User;

public interface UserRepository {
    User save(User user);
    List<User> saveAll(List<User> userList);

    boolean existsById(String id);
    boolean existsByEmail(String email);

    User findById(String id);
    User findByEmail(String email);
    List<User> findAll();

    boolean deleteById(String id);
    long count();
    void deleteAll();

    User update(User user);
}
