package com.shopping.repository;

import java.util.List;
import java.util.ArrayList;

import com.shopping.model.User;

import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

/**
 * 사용자 데이터 접근 담당하는 Repository 클래스
 * 계정(사용자, 관리자)의 저장/조회/중복검사 전담
 * CRUD 기능 제공
 */
public class UserRepository {

    // 파일명 상수
    private static final String FILE_NAME = Constants.USER_DATA_FILE;
    
    // 단일 사용자 저장 (이메일 중복 방지)
    public User save(User user) {
        // 이메일 중복 검사
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + user.getEmail());
        }
        
        // 기존 사용자 목록 조회
        List<User> users = FileManager.readFromFile(FILE_NAME);
        
        // 기존 사용자 업데이트 또는 새 사용자 추가
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
        
        // 파일에 저장
        FileManager.writeToFile(FILE_NAME, users);
        
        return user;
    }
    
    // 여러 사용자 일괄 저장
    public List<User> saveAll(List<User> userList) {
        List<User> savedUsers = new ArrayList<>();
        
        for (User user : userList) {
            try {
                User savedUser = save(user);
                savedUsers.add(savedUser);
            } catch (IllegalArgumentException e) {
                System.err.println("저장 실패: " + e.getMessage());
                // 실패한 경우에도 계속 진행하거나, 전체 실패로 처리할지는 정책에 따라 결정
            }
        }
        
        return savedUsers;
    }

    // ID로 존재 여부 확인
    public boolean existsById(String id) {
        return findById(id) != null;
    }
    
    // 이메일로 존재 여부 확인
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    // ID로 사용자 조회
    public User findById(String id) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
                
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // 이메일로 사용자 조회 (대소문자 구분 없음)
    public User findByEmail(String email) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // 전체 사용자 조회
    public List<User> findAll() {
        return FileManager.readFromFile(FILE_NAME);
    }

    // ID로 사용자 삭제
    public boolean deleteById(String id) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) {
            FileManager.writeToFile(FILE_NAME, users);
        }
        return removed;
    }
    
    // 사용자 수 조회
    public long count() {
        return findAll().size();
    }
    
    // 모든 사용자 삭제
    public void deleteAll() {
        FileManager.writeToFile(FILE_NAME, new ArrayList<User>());
    }
    
    // 이메일 중복 검사를 위한 별도 메서드 (save에서 사용)
    private boolean isDuplicateEmail(String email, String excludeUserId) {
        List<User> users = FileManager.readFromFile(FILE_NAME);
        return users.stream()
                .filter(u -> !u.getId().equals(excludeUserId)) // 본인 제외
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }
    
    // 업데이트용 save 메서드 (이메일 중복 검사 시 본인 제외)
    public User update(User user) {
        if (isDuplicateEmail(user.getEmail(), user.getId())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + user.getEmail());
        }
        
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