package com.shopping.service;

import com.shopping.model.Product;
import com.shopping.repository.ProductRepository; // ProductRepository 임포트
import java.util.List;
import java.util.Optional;

/**
 * 상품 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * ProductController와 ProductRepository 간의 중재자 역할을 합니다.
 */
public class ProductService {

    // [수정됨] HashMap 대신 ProductRepository를 사용
    private final ProductRepository productRepository;

    // [수정됨] 생성자를 통해 외부에서 ProductRepository 구현체를 주입받음
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 모든 상품 목록을 반환합니다.
     */
    public List<Product> getAllProducts() {
        // [수정됨] Repository에 작업 위임
        return productRepository.findAll();
    }

    /**
     * 주어진 ID에 해당하는 상품을 검색하여 Optional<Product>로 반환합니다.
     */
    public Optional<Product> getProductById(String id) {
        // [수정됨] Repository에 작업 위임
        return productRepository.findById(id);
    }
    
    /**
     * 이름으로 상품을 검색합니다.
     */
    public List<Product> findProductsByName(String name) {
        // [수정됨] Repository에 작업 위임 (수정된 메소드 이름 사용)
        return productRepository.findByNameContains(name);
    }

    /**
     * 새로운 상품을 추가합니다.
     * @return 새로 생성된 Product 객체
     * @throws IllegalArgumentException 유효성 검사 실패 시 발생
     */
    public Product addProduct(Product product) {
        validateProductData(product.getName(), product.getPrice(), product.getStock());
        // 이름 중복 검사 등 추가적인 비즈니스 로직이 필요하면 여기에 구현
        
        // [수정됨] Repository에 작업 위임
        return productRepository.save(product);
    }

    /**
     * 기존 상품의 정보를 수정합니다.
     * @return 수정된 Product 객체
     * @throws IllegalArgumentException 상품을 찾을 수 없거나 유효성 검사 실패 시 발생
     */
    public Product updateProduct(Product product) {
        // 수정할 상품이 존재하는지 먼저 확인
        productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + product.getId() + "에 해당하는 상품을 찾을 수 없습니다."));

        validateProductData(product.getName(), product.getPrice(), product.getStock());
        
        // [수정됨] Repository에 작업 위임 (save는 신규/수정 모두 처리)
        return productRepository.save(product);
    }
    
    /**
     * 특정 상품을 삭제합니다.
     * @return 삭제 성공 시 true, 실패 시 false
     */
    public boolean deleteProduct(String id) {
        // [수정됨] Repository에 작업 위임
        return productRepository.deleteById(id);
    }

    /**
     * 상품의 재고를 추가합니다.
     * @throws IllegalArgumentException 상품을 찾을 수 없거나 추가할 재고가 0 이하일 경우
     */
    public void addStock(String id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 재고는 0보다 커야 합니다.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + id + "에 해당하는 상품을 찾을 수 없습니다."));
        
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }

    // --- 유효성 검사를 위한 Private Helper Method ---
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
}