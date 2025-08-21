package com.shopping.service;

import com.shopping.model.Product;
import com.shopping.model.ProductCategory;
import com.shopping.repository.FileProductRepository;
import java.util.List;
import java.util.Optional;

/**
 * 상품 관련 비즈니스 로직을 처리하고 데이터 유효성을 검증하는 서비스 클래스.
 * Controller와 Repository 간의 중재자 역할을 합니다.
 */
public class ProductService {

    private final FileProductRepository productRepository;

    public ProductService(FileProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 새로운 상품을 추가합니다. 저장 전 데이터 유효성을 검증합니다.
     * @param product 추가할 상품 객체
     * @return 저장되고 ID가 부여된 상품 객체
     */
    public Product addProduct(Product product) {
        validateProductData(product);
        return productRepository.save(product);
    }

    /**
     * 기존 상품의 정보를 수정합니다. 저장 전 데이터 유효성을 검증합니다.
     * @param product 수정할 상품 객체
     * @return 수정된 상품 객체
     */
    public Product updateProduct(Product product) {
        // 수정 전, 해당 ID의 상품이 존재하는지 먼저 확인
        productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + product.getId() + "에 해당하는 상품을 찾을 수 없습니다."));
        validateProductData(product);
        return productRepository.save(product);
    }
    
    /**
     * 특정 상품을 삭제합니다.
     * @param id 삭제할 상품의 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteProduct(String id) {
        return productRepository.deleteById(id);
    }

    /**
     * 상품의 재고를 추가합니다. (입고 처리)
     * @param id 재고를 추가할 상품의 ID
     * @param quantity 추가할 수량
     */
    public void addStock(String id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("추가할 재고는 0보다 커야 합니다.");
        }
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("오류: ID " + id + "에 해당하는 상품을 찾을 수 없습니다."));
        
        int newStock = product.getStock() + quantity;
        if (newStock > 9999) {
            throw new IllegalArgumentException("오류: 재고는 9999개를 초과할 수 없습니다.");
        }
        product.setStock(newStock);
        productRepository.save(product);
    }

    /**
     * 페이지 번호에 해당하는 상품 목록을 반환합니다.
     * @param page 페이지 번호
     * @param pageSize 페이지당 상품 수
     * @return 페이징 처리된 상품 목록
     */
    public List<Product> getAllProducts(int page, int pageSize) {
        return productRepository.findAll(page, pageSize);
    }
    
    /**
     * 전체 상품의 개수를 반환합니다.
     * @return 전체 상품 개수
     */
    public long getTotalProductCount() {
        return productRepository.countAll();
    }

    /**
     * 판매량이 높은 상위 상품 목록을 반환합니다.
     * @param limit 조회할 상품 개수
     * @return 베스트셀러 상품 목록
     */
    public List<Product> getBestSellers(int limit) {
        return productRepository.findBestSellers(limit);
    }

    /**
     * 최근에 등록된 신상품 목록을 반환합니다.
     * @param limit 조회할 상품 개수
     * @return 신상품 목록
     */
    public List<Product> getNewArrivals(int limit) {
        return productRepository.findNewArrivals(limit);
    }
    
    /**
     * 특정 카테고리에 해당하는 상품 목록을 반환합니다.
     * @param category 조회할 카테고리명
     * @return 해당 카테고리의 상품 목록
     */
    public List<Product> findProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }
    
    /**
     * 지정된 가격 범위 내의 상품 목록을 반환합니다.
     * @param min 최소 가격
     * @param max 최대 가격 (-1인 경우 무제한)
     * @return 해당 가격대의 상품 목록
     */
    public List<Product> findProductsByPriceRange(double min, double max) {
        return productRepository.findByPriceRange(min, max);
    }

    /**
     * 상품 ID로 특정 상품을 조회합니다.
     * @param id 조회할 상품의 ID
     * @return Optional로 감싸진 상품 객체
     */
    public Optional<Product> findProductById(String id) {
        return productRepository.findById(id);
    }

    /**
     * 상품명에 특정 키워드가 포함된 상품 목록을 검색합니다.
     * @param name 검색할 상품명 키워드
     * @return 검색된 상품 목록
     */
    public List<Product> findProductsByName(String name) {
        return productRepository.findBynameContains(name);
    }

    /**
     * 상품 데이터의 유효성을 검증하는 private 헬퍼 메소드.
     * 요구사항에 명시된 모든 제약 조건을 검사합니다.
     * @param product 검증할 상품 객체
     * @throws IllegalArgumentException 유효성 검증 실패 시 예외 발생
     */
    private void validateProductData(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("오류: 상품 이름은 비어 있을 수 없습니다.");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("오류: 상품 이름은 100자를 초과할 수 없습니다.");
        }
        if (product.getPrice() < 0 || product.getPrice() > 10_000_000) {
            throw new IllegalArgumentException("오류: 상품 가격은 0원 이상 10,000,000원 이하이어야 합니다.");
        }
        if (product.getStock() < 0 || product.getStock() > 9999) {
            throw new IllegalArgumentException("오류: 재고 수량은 0개 이상 9999개 이하이어야 합니다.");
        }
        if (product.getDescription() != null && product.getDescription().length() > 500) {
            throw new IllegalArgumentException("오류: 상품 설명은 500자를 초과할 수 없습니다.");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("오류: 카테고리는 반드시 지정해야 합니다.");
        }
    }
}