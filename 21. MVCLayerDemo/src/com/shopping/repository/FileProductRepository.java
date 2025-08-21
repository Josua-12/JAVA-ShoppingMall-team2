package com.shopping.repository;

import com.shopping.model.Product;
import com.shopping.persistence.FileManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 상품 데이터의 영속성을 관리하는 저장소 클래스.
 * FileManager를 사용하여 파일에서 데이터를 읽고 쓰는 역할을 담당합니다.
 * ProductRepository 인터페이스의 파일 기반 구현체입니다.
 */
public class FileProductRepository implements ProductRepository {

    private final Map<String, Product> productStore = new HashMap<>();
    private static final String DATA_FILE_NAME = "products.dat";
    private long sequence = 0L;

    public FileProductRepository() {
        loadDataFromFile();
    }

    private void loadDataFromFile() {
        List<Product> products = FileManager.readFromFile(DATA_FILE_NAME);
        for (Product product : products) {
            productStore.put(product.getId(), product);
        }
        this.sequence = productStore.keySet().stream()
                .map(id -> id.substring(1))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0L);
    }

    private void saveDataToFile() {
        FileManager.writeToFile(DATA_FILE_NAME, new ArrayList<>(productStore.values()));
    }

    /*
     * [수정] 신규 상품(ID가 null)인 경우 ID와 등록일시를 자동 생성하여 저장하도록 로직을 수정했습니다.
     */
    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            String newId = generateId();
            product.setId(newId);
            product.setRegistrationDateTime(LocalDateTime.now());
        }
        productStore.put(product.getId(), product);
        saveDataToFile();
        return product;
    }

    @Override
    public void saveAll(Product product) {
        if (product == null) return;
        save(product);
    }

    @Override
    public Optional<Product> findById(String productId) {
        return Optional.ofNullable(productStore.get(productId));
    }

    @Override
    public List<Product> findBynameContains(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        String lowerCaseName = name.toLowerCase();
        return productStore.values().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerCaseName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return new ArrayList<>();
        }
        return productStore.values().stream()
                .filter(p -> p.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        return productStore.values().stream()
                .sorted(Comparator.comparing(Product::getId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(String productId) {
        if (productStore.remove(productId) != null) {
            saveDataToFile();
            return true;
        }
        return false;
    }

    /**
     * "P001" 형식의 새로운 상품 ID를 생성하여 반환합니다.
     */
    public String generateId() {
        this.sequence++;
        return String.format("P%03d", this.sequence);
    }

    // =================================================================
    // [추가] 인터페이스에 새로 추가된 메소드들의 실제 구현
    // =================================================================

    @Override
    public List<Product> findAll(int page, int pageSize) {
        return productStore.values().stream()
                .sorted(Comparator.comparing(Product::getId))
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    @Override
    public long countAll() {
        return productStore.size();
    }

    @Override
    public List<Product> findBestSellers(int limit) {
        return productStore.values().stream()
                .sorted(Comparator.comparingInt(Product::getSalesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findNewArrivals(int limit) {
        return productStore.values().stream()
                .sorted(Comparator.comparing(Product::getRegistrationDateTime).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByPriceRange(int minPrice, int maxPrice) {
        return productStore.values().stream()
                .filter(p -> p.getPrice() >= minPrice && (maxPrice == -1 || p.getPrice() < maxPrice))
                .sorted(Comparator.comparing(Product::getPrice))
                .collect(Collectors.toList());
    }
}