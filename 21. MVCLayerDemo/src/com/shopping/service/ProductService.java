package com.shopping.service;

import com.shopping.model.Product;
import com.shopping.repository.FileProductRepository; // FileProductRepository 임포트
import java.util.List;
import java.util.Optional;

/**
 * 상품 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * ProductController와 FileProductRepository 간의 중재자 역할을 합니다.
 */
public class ProductService {

    // [수정됨] 변수명도 fileProductRepository로 변경
    private final FileProductRepository fileProductRepository;

    // [수정됨] 생성자를 통해 외부에서 FileProductRepository 구현체를 주입받음
    public ProductService(FileProductRepository fileProductRepository) {
        this.fileProductRepository = fileProductRepository;
    }

    /**
     * 모든 상품 목록을 반환합니다.
     */
    public List<Product> getAllProducts() {
        return fileProductRepository.findAll();
    }

    /**
     * 주어진 ID에 해당하는 상품을 검색하여 Optional<Product>로 반환합니다.
     */
    public Optional<Product> getProductById(String id) {
        return fileProductRepository.findById(id);
    }
    
    /**
     * 이름으로 상품을 검색합니다.
     */
    public List<Product> findProductsByName(String name) {
        return fileProductRepository.findBynameContains(name);
    }

    /**
     * 새로운 상품을 추가합니다.
     */
    public Product addProduct(Product product) {
        validateProductData(product.getName(), product.getPrice(), product.getStock());
        return fileProductRepository.save(product);
    }

    /**
     * 기존 상품의 정보를 수정합니다.
     */
    public Product updateProduct(Product product) {
        fileProductRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + product.getId() + "에 해당하는 상품을 찾을 수 없습니다."));

        validateProductData(product.getName(), product.getPrice(), product.getStock());
        return fileProductRepository.save(product);
    }
    
    /**
     * 특정 상품을 삭제합니다.
     */
    public boolean deleteProduct(String id) {
        return fileProductRepository.deleteById(id);
    }

    /**
     * 상품의 재고를 추가합니다.
     */
    public void addStock(String id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 재고는 0보다 커야 합니다.");
        }
        Product product = fileProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + id + "에 해당하는 상품을 찾을 수 없습니다."));
        
        product.setStock(product.getStock() + quantity);
        fileProductRepository.save(product);
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
