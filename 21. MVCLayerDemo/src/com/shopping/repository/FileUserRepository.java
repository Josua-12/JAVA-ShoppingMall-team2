package com.shopping.repository;

import java.util.List;
import java.util.ArrayList;

import com.shopping.model.User;
import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

public class FileUserRepository implements UserRepository {
    
    private static final String FILE_NAME = Constants.USER_DATA_FILE;

    @Override
    public User save(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + user.getEmail());
        }
        List<User> users = FileManager.readFromFile(FILE_NAME);

        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                updated = true;
                break;
            }
        }
        if (!updated) {
            users.add(user);
        }
        FileManager.writeToFile(FILE_NAME, users);
        return user;
    }

    @Override
    public List<User> saveAll(List<User> userList) {
        List<User> savedUsers = new ArrayList<>();
        for (User user : userList) {
            try {
                savedUsers.add(save(user));
            } catch (IllegalArgumentException e) {
                System.err.println("저장 실패: " + e.getMessage());
            }
        }
        return savedUsers;
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
    public User findById(String id) {
        return FileManager.readFromFile(FILE_NAME).stream()
                .filter(obj -> obj instanceof User)   // User인 것만
                .map(obj -> (User) obj)               // 캐스팅
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }


    @Override
    public User findByEmail(String email) {
        return (User) FileManager.readFromFile(FILE_NAME).stream()
        		.filter(obj -> obj instanceof User)   // User인 것만
                .map(obj -> (User) obj)               // 캐스팅
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return FileManager.readFromFile(FILE_NAME);
    }

    @Override
    public boolean deleteById(String id) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            FileManager.writeToFile(FILE_NAME, users);
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
    public User update(User user) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                FileManager.writeToFile(FILE_NAME, users);
                return user;
            }
        }
        throw new IllegalArgumentException("업데이트할 사용자를 찾을 수 없습니다: " + user.getId());
    }
}
