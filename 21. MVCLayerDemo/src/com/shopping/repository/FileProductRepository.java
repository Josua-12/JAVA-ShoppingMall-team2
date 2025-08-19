package com.shopping.repository;

import com.shopping.model.Product;
import com.shopping.persistence.FileManager;

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

    // 상품 데이터를 메모리에 저장하여 빠른 조회를 지원 (ID를 Key로 사용)
    private final Map<String, Product> productStore = new HashMap<>();
    private static final String DATA_FILE_NAME = "products.dat";
    private long sequence = 0L; // 상품 ID 생성을 위한 시퀀스

    /**
     * FileProductRepository 생성자.
     * 애플리케이션 시작 시 파일에서 데이터를 읽어와 메모리에 로드하고,
     * ID 생성을 위한 시퀀스를 초기화합니다.
     */
    public FileProductRepository() {
        loadDataFromFile();
    }

    /**
     * 파일에서 상품 데이터를 읽어와 메모리(productStore)에 적재하고,
     * ID 시퀀스를 가장 큰 ID를 기준으로 초기화합니다.
     */
    private void loadDataFromFile() {
        List<Product> products = FileManager.readFromFile(DATA_FILE_NAME);
        for (Product product : products) {
            productStore.put(product.getId(), product);
        }
        // ID 시퀀스 초기화 (가장 큰 ID 번호를 찾아 설정)
        this.sequence = productStore.keySet().stream()
                .map(id -> id.substring(1)) // "P" 제외
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0L); // 상품이 없으면 0으로 시작
    }

    /**
     * 현재 메모리에 있는 상품 데이터를 파일에 저장합니다.
     * 데이터에 변경이 있을 때마다 호출되어야 합니다.
     */
    private void saveDataToFile() {
        FileManager.writeToFile(DATA_FILE_NAME, new ArrayList<>(productStore.values()));
    }

    @Override
    public Product save(Product product) {
        productStore.put(product.getId(), product);
        saveDataToFile();
        return product;
    }

    /**
     * 인터페이스의 saveAll(Product products) 시그니처에 맞게 수정합니다.
     * 단일 상품을 저장하는 save 메소드를 호출합니다.
     * 참고: saveAll이라는 이름과 달리 단일 객체만 처리하는 것은 인터페이스 설계상 개선이 필요할 수 있습니다.
     */
    @Override
    public void saveAll(Product product) {
        if (product == null) return;
        // 단일 상품을 저장하는 로직으로 변경
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
                .filter(product -> product.getName().toLowerCase().contains(lowerCaseName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategory(String category) {
        if (category == null || category.isBlank()) {
            return new ArrayList<>();
        }
        return productStore.values().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findAll() {
        // ID 순서대로 정렬하여 반환
        ArrayList<Product> productList = new ArrayList<>(productStore.values());
        productList.sort(Comparator.comparing(Product::getId));
        return productList;
    }

    @Override
    public boolean deleteById(String productId) {
        Product removedProduct = productStore.remove(productId);
        if (removedProduct != null) {
            saveDataToFile();
            return true;
        }
        return false;
    }

    /**
     * "P001" 형식의 새로운 상품 ID를 생성하여 반환합니다.
     * 이 메소드는 인터페이스에 포함되지 않은 구현체 고유의 기능입니다.
     * @return 새로 생성된 상품 ID 문자열
     */
    public String generateId() {
        this.sequence++;
        return String.format("P%03d", this.sequence);
    }
}
