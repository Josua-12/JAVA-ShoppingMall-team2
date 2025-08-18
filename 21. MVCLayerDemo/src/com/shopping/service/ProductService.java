package com.shopping.service;

import com.shopping.model.Product; // Product 모델 클래스 임포트

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // 동시성 환경에서 ID 생성을 위한 AtomicInteger

/**
 * 상품 관련 비즈니스 로직 및 데이터 관리를 담당하는 서비스 클래스입니다.
 * 상품의 CRUD (생성, 조회, 수정, 삭제) 작업과 재고/가격 유효성 검사 규칙을 포함합니다.
 * <p>
 * 설계 요구사항:
 * 1. 상품 CRUD 및 재고/가격 유효성 규칙 구현
 * 2. 도메인 규칙("상품은 음수 재고/가격 불가") 강제
 * 3. ProductController와 Repository 간의 비즈니스 로직 분리 (중복 방지, 일관성 유지)
 * </p>
 */
public class ProductService {

    // 상품 데이터를 저장하기 위한 메모리 내 저장소 (실제 환경에서는 데이터베이스 연동)
    // Key: 상품 ID, Value: Product 객체
    private final Map<Integer, Product> products = new HashMap<>();

    // 상품 ID를 생성하기 위한 AtomicInteger. 동시성 환경에서도 안전하게 고유 ID를 생성합니다.
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * ProductService 생성자입니다.
     * 서비스 초기화를 위해 몇 가지 샘플 데이터를 추가합니다.
     */
    public ProductService() {
        // 초기 샘플 데이터 추가
        addProduct("노트북", 1200000, 10, "전자기기");
        addProduct("스마트폰", 900000, 20, "전자기기");
        addProduct("키보드", 80000, 50, "주변기기");
        addProduct("마우스", 40000, 100, "주변기기");
    }

    /**
     * 설계 포인트 1: list
     * 모든 상품 목록을 반환합니다.
     *
     * return 등록된 모든 상품의 리스트
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    /**
     * 설계 포인트 1: find
     * 주어진 ID에 해당하는 상품을 검색하여 반환합니다.
     *
     * param id 검색할 상품의 고유 ID
     * return 찾아낸 Product 객체
     * throws IllegalArgumentException ID에 해당하는 상품을 찾을 수 없을 경우 발생
     */
    public Product findProductById(int id) {
        Product product = products.get(id);
        if (product == null) {
            throw new IllegalArgumentException("오류: ID " + id + "에 해당하는 상품을 찾을 수 없습니다.");
        }
        return product;
    }

    /**
     * 설계 포인트 1: add
     * 새로운 상품을 추가합니다. 카테고리는 기본값("기타")으로 설정됩니다.
     * 이 메서드는 Controller에서 카테고리 정보 없이 상품을 추가할 때 사용됩니다.
     *
     * param name  상품 이름
     * param price 상품 가격
     * param stock 초기 재고 수량
     * return 새로 생성된 Product 객체
     * throws IllegalArgumentException 유효성 검사(이름 중복, 가격/재고 유효성) 실패 시 발생
     */
    public Product addProduct(String name, double price, int stock) {
        return addProduct(name, price, stock, "기타"); // 카테고리 기본값 설정
    }

    /**
     * 설계 포인트 1: add (내부적으로 카테고리를 포함한 상품 추가)
     * 카테고리를 포함하여 새로운 상품을 추가합니다.
     * 이 메서드는 상품 데이터의 유효성을 검사하고, 고유 ID를 할당한 후 상품을 저장합니다.
     *
     * param name     상품 이름
     * param price    상품 가격
     * param stock    초기 재고 수량
     * param category 상품 카테고리
     * return 새로 생성된 Product 객체
     * throws IllegalArgumentException 유효성 검사(이름 중복, 가격/재고 유효성) 실패 시 발생
     */
    private Product addProduct(String name, double price, int stock, String category) {
        // 설계 포인트 2: 검증 - 가격 > 0, 재고 >= 0, 이름 비어있지 않음
        validateProductData(name, price, stock);
        // 설계 포인트 2: 검증 - ID를 이용한 이름 중복 검사 (새 상품이므로 -1을 전달하여 모든 기존 상품과 비교)
        validateDuplicateName(name, -1);

        int id = nextId.getAndIncrement(); // 고유 ID 생성
        Product newProduct = new Product(id, name, price, stock, category);
        products.put(id, newProduct); // 상품 저장
        return newProduct;
    }

    /*
     * 설계 포인트 1: update
     * 기존 상품의 정보를 수정합니다 (이름, 가격).
     *
     * param id        수정할 상품의 고유 ID
     * param newName   새로운 상품 이름
     * param newPrice  새로운 상품 가격
     * throws IllegalArgumentException 상품을 찾을 수 없거나 유효성 검사 실패 시 발생
     */
    public void updateProduct(int id, String newName, double newPrice) {
        Product product = findProductById(id); // 상품 존재 여부 확인 (없으면 여기서 IllegalArgumentException 발생)

        // 설계 포인트 2: 검증 - 가격 > 0
        if (newPrice <= 0) {
            throw new IllegalArgumentException("오류: 상품 가격은 0보다 커야 합니다.");
        }
        // 이름 유효성 검사 (공백 제외)
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("오류: 상품 이름은 비어 있을 수 없습니다.");
        }
        // 설계 포인트 2: 검증 - ID를 이용한 이름 중복 검사 (자기 자신을 제외하고 중복 검사)
        validateDuplicateName(newName, id);

        // 유효성 검사 통과 시 정보 업데이트
        product.setName(newName);
        product.setPrice(newPrice);
    }
    
    /*
     * 설계 포인트 1: delete
     * 특정 상품을 삭제합니다.
     *
     * param id 삭제할 상품의 고유 ID
     * throws IllegalArgumentException ID에 해당하는 상품을 찾을 수 없을 경우 발생
     */
    public void deleteProduct(int id) {
        if (products.remove(id) == null) { // Map에서 ID를 키로 상품 삭제
            throw new IllegalArgumentException("오류: ID " + id + "에 해당하는 상품을 찾을 수 없습니다.");
        }
    }

    /*
     * 설계 포인트 1: changeStock
     * 상품의 재고 수량을 변경합니다.
     *
     * param id     재고를 변경할 상품의 고유 ID
     * param amount 변경할 수량 (양수: 재고 증가, 음수: 재고 감소)
     * throws IllegalArgumentException 상품을 찾을 수 없거나 재고가 0 미만이 될 경우 발생
     */
    public void changeStock(int id, int amount) {
        Product product = findProductById(id); // 상품 존재 여부 확인

        int newStock = product.getStock() + amount;
        // 설계 포인트 2: 검증 - 재고 >= 0
        if (newStock < 0) {
            throw new IllegalArgumentException("오류: 재고는 0 미만이 될 수 없습니다. (현재 재고: " + product.getStock() + ")");
        }
        product.setStock(newStock); // 재고 업데이트
    }

    // --- Private Helper Methods for Validation (설계 포인트 2 구현) ---

    /*
     * 상품 데이터(이름, 가격, 초기 재고)의 기본 유효성을 검사합니다.
     *
     * param name  상품 이름
     * param price 상품 가격
     * param stock 초기 재고 수량
     * throws IllegalArgumentException 유효성 검사 실패 시 발생
     */
    private void validateProductData(String name, double price, int stock) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("오류: 상품 이름은 비어 있을 수 없습니다.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("오류: 상품 가격은 0보다 커야 합니다.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("오류: 재고는 0 이상이어야 합니다.");
        }
    }

    /*
     * 상품 이름의 중복 여부를 검사합니다.
     *
     * param name             검사할 상품 이름
     * param currentProductId 현재 수정 중인 상품의 ID (새 상품 추가 시에는 -1 전달)
     * throws IllegalArgumentException 동일한 이름의 상품이 이미 존재할 경우 발생
     */
    private void validateDuplicateName(String name, int currentProductId) {
        for (Product p : products.values()) {
            // 다른 상품(ID가 다름)이면서 이름이 같은 경우 중복으로 간주
            if (p.getId() != currentProductId && p.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("오류: '" + name + "' 이름의 상품이 이미 존재합니다.");
            }
        }
    }

}