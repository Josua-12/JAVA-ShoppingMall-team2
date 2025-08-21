package com.shopping.repository;

import java.util.List;
import java.util.ArrayList;

import com.shopping.model.Admin;
import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

public class FileAdminRepository implements AdminRepository {
    
    private static final String FILE_NAME = Constants.ADMIN_DATA_FILE;

    @Override
    public Admin save(Admin admin) {
    	List<Admin> admins = FileManager.readFromFile(FILE_NAME);
        
    	if (existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + admin.getEmail());
        }
        
        boolean updated = false;
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(admin.getId())) {
            	admins.set(i, admin);
                updated = true;
                break;
            }
        }
        if (!updated) {
        	admins.add(admin);
        }
        FileManager.writeToFile(FILE_NAME, admins);
        return admin;
    }

    @Override
    public List<Admin> saveAll(List<Admin> adminList) {
        List<Admin> savedAdmins = new ArrayList<>();
        for (Admin admin : adminList) {
            try {
                savedAdmins.add(save(admin));
            } catch (IllegalArgumentException e) {
                System.err.println("저장 실패: " + e.getMessage());
            }
        }
        return savedAdmins;
    }

    @Override
    public boolean existsById(String id) {
        return findById(id) != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public Admin findById(String id) {
        return FileManager.readFromFile(FILE_NAME).stream()
                .filter(obj -> obj instanceof Admin)   
                .map(obj -> (Admin) obj)              
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    @Override
    public Admin findByEmail(String email) {
        return (Admin) FileManager.readFromFile(FILE_NAME).stream()
        		.filter(obj -> obj instanceof Admin)   
                .map(obj -> (Admin) obj)               
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Admin> findAll() {
        return FileManager.readFromFile(FILE_NAME);
    }

    @Override
    public boolean deleteById(String id) {
        List<Admin> admins = FileManager.readFromFile(FILE_NAME);
        boolean removed = admins.removeIf(u -> u.getId().equals(id));
        if (removed) {
            FileManager.writeToFile(FILE_NAME, admins);
        }
        return removed;
    }

    @Override
    public long count() {
        return findAll().size();
    }

    @Override
    public void deleteAll() {
        FileManager.writeToFile(FILE_NAME, new ArrayList<>());
    }

    @Override
    public Admin update(Admin admin) {
        List<Admin> admins = FileManager.readFromFile(FILE_NAME);
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).getId().equals(admin.getId())) {
            	admins.set(i, admin);
                FileManager.writeToFile(FILE_NAME, admins);
                return admin;
            }
        }
        throw new IllegalArgumentException("업데이트할 사용자를 찾을 수 없습니다: " + admin.getId());
    }
}
