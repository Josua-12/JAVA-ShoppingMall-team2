package com.shopping.repository;

import java.util.List;

import com.shopping.model.Admin;

public interface AdminRepository {
    Admin save(Admin user);
    List<Admin> saveAll(List<Admin> userList);

    boolean existsById(String id);
    boolean existsByEmail(String email);

    Admin findById(String id);
    Admin findByEmail(String email);
    List<Admin> findAll();

    boolean deleteById(String id);
    long count();
    void deleteAll();

    Admin update(Admin user);
}
