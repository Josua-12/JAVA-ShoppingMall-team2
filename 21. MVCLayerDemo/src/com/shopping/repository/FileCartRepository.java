package com.shopping.repository;

import com.shopping.model.Cart;
import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileCartRepository {

    // 모든 사용자의 장바구니 정보를 담을 파일 이름
    private static final String FILE_NAME = Constants.CART_DATA_FILE;
    private Map<String, Cart> database;

    
    public FileCartRepository() {
        loadData();
    }


    //옵셔널을 이용한 유저아이디로 카트찾는 메서드
    public Optional<Cart> findByUserId(String userId) {
        return Optional.ofNullable(database.get(userId));
    }


    public void save(Cart cart) {
        database.put(cart.getUserId(), cart);
        saveData();
    }


    //특정아이디의 카트 삭제
    public void deleteByUserId(String userId) {
        database.remove(userId);
        saveData();
    }

    // --- FileManager를 사용하는 private 헬퍼 메서드 ---
    private void saveData() {
        List<Cart> cartList = new ArrayList<>(database.values());
        FileManager.writeToFile(FILE_NAME, cartList);
    }

    /**
     * 파일에서 데이터를 불러와 메모리의 Map을 초기화합니다.
     */
    private void loadData() {
        // FileManager를 통해 파일에서 Cart 리스트를 불러옵니다.
        List<Cart> cartList = FileManager.readFromFile(FILE_NAME);
        
        // 불러온 List를 userId를 key로 하는 Map으로 변환하여 database를 구성합니다.
        this.database = new HashMap<>();
        for (Cart cart : cartList) {
            this.database.put(cart.getUserId(), cart);
        }
    }
}