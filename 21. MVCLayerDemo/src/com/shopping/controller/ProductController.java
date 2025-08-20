package com.shopping.controller;

import com.shopping.model.Product;
import com.shopping.repository.FileProductRepository;
import com.shopping.service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 상품 관련 요청을 처리하고 ProductService와 통신하여 비즈니스 로직을 실행하는 컨트롤러 클래스입니다.
 * 콘솔 기반의 입력/출력 예제 버전입니다.
 */
public class ProductController {

    private final ProductService productService;
    private final Scanner scanner;

    public ProductController(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
    }

    /** 상품 목록 출력 */
    public void listProducts() {
        System.out.println("\n--- 상품 목록 ---");
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("등록된 상품이 없습니다.");
        } else {
            System.out.printf("%-20s %-20s %-10s %-5s %-10s\n", "ID", "이름", "가격", "재고", "카테고리");
            System.out.println("-------------------------------------------------------------------");
            for (Product product : products) {
                System.out.printf("%-20s %-20s %-10.0f %-5d %-10s\n",
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getCategory());
            }
        }
    }

    /** 상품 상세 조회 */
    public void viewProductDetail() {
        System.out.println("\n--- 상품 상세 조회 ---");
        System.out.print("조회할 상품의 ID를 입력하세요: ");
        String id = scanner.nextLine();

        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            System.out.println("\n--- 상품 정보 ---");
            System.out.println("ID: " + product.getId());
            System.out.println("이름: " + product.getName());
            System.out.println("가격: " + String.format("%,.0f원", product.getPrice()));
            System.out.println("재고: " + product.getStock() + "개");
            System.out.println("카테고리: " + product.getCategory());
        } else {
            System.out.println("해당 ID의 상품이 존재하지 않습니다.");
        }
    }

    /** 새 상품 등록 */
    public void addProduct() {
        System.out.println("\n--- 새 상품 등록 ---");
        try {
            System.out.print("상품 이름: ");
            String name = scanner.nextLine();

            System.out.print("상품 가격: ");
            double price = Double.parseDouble(scanner.nextLine());

            System.out.print("초기 재고: ");
            int stock = Integer.parseInt(scanner.nextLine());

            System.out.print("상품 카테고리: ");
            String category = scanner.nextLine();

            // ID는 Repository에서 자동 생성
            Product product = new Product(null, name, price, stock, category);

            Product newProduct = productService.addProduct(product);
            System.out.println("상품 '" + newProduct.getName() + "' (ID: " + newProduct.getId() + ") 이(가) 등록되었습니다.");
        } catch (NumberFormatException e) {
            System.out.println("오류: 가격과 재고는 숫자로 입력하세요.");
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /** 상품 정보 수정 */
    public void updateProduct() {
        System.out.println("\n--- 상품 정보 수정 ---");
        System.out.print("수정할 상품의 ID를 입력하세요: ");
        String id = scanner.nextLine();

        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isEmpty()) {
            System.out.println("해당 ID의 상품이 존재하지 않습니다.");
            return;
        }
        Product currentProduct = productOpt.get();

        try {
            System.out.print("새 상품 이름 (현재: " + currentProduct.getName() + ", 변경 없으면 엔터): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) currentProduct.setName(newName);

            System.out.print("새 상품 가격 (현재: " + currentProduct.getPrice() + ", 변경 없으면 엔터): ");
            String priceInput = scanner.nextLine().trim();
            if (!priceInput.isEmpty()) currentProduct.setPrice(Double.parseDouble(priceInput));

            productService.updateProduct(currentProduct);
            System.out.println("ID " + id + " 상품 정보가 수정되었습니다.");
        } catch (NumberFormatException e) {
            System.out.println("오류: 가격은 숫자로 입력하세요.");
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /** 상품 삭제 */
    public void deleteProduct() {
        System.out.println("\n--- 상품 삭제 ---");
        System.out.print("삭제할 상품의 ID를 입력하세요: ");
        String id = scanner.nextLine();

        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                System.out.println("ID " + id + " 상품이 삭제되었습니다.");
            } else {
                System.out.println("해당 ID의 상품이 존재하지 않습니다.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /** 상품 재고 변경 */
    public void changeProductStock() {
        System.out.println("\n--- 상품 재고 변경 (관리자) ---");
        try {
            System.out.print("상품 ID: ");
            String id = scanner.nextLine();

            System.out.print("추가할 수량 입력: ");
            int amount = Integer.parseInt(scanner.nextLine());

            productService.addStock(id, amount);
            Optional<Product> productOpt = productService.findProductById(id);
            productOpt.ifPresent(product ->
                    System.out.println("ID " + id + " 상품 재고가 변경되었습니다. (현재 재고: " + product.getStock() + ")")
            );
        } catch (NumberFormatException e) {
            System.out.println("오류: 수량은 숫자로 입력하세요.");
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /** 콘솔 메뉴 실행 */
    public void startConsoleMenu() {
        int choice;
        do {
            System.out.println("\n============================");
            System.out.println("    상품 관리 시스템 메뉴");
            System.out.println("============================");
            System.out.println("1. 상품 목록 조회");
            System.out.println("2. 상품 상세 조회");
            System.out.println("3. 새 상품 등록");
            System.out.println("4. 상품 정보 수정");
            System.out.println("5. 상품 삭제");
            System.out.println("6. 상품 재고 변경 (관리자)");
            System.out.println("0. 종료");
            System.out.print("메뉴 선택: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> listProducts();
                    case 2 -> viewProductDetail();
                    case 3 -> addProduct();
                    case 4 -> updateProduct();
                    case 5 -> deleteProduct();
                    case 6 -> changeProductStock();
                    case 0 -> System.out.println("시스템을 종료합니다.");
                    default -> System.out.println("잘못된 메뉴 선택입니다. 0-6 사이 숫자를 입력하세요.");
                }
            } catch (NumberFormatException e) {
                System.out.println("오류: 유효한 숫자 메뉴를 입력하세요.");
                choice = -1;
            }
        } while (choice != 0);
    }

    /** 실행 진입점 */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileProductRepository repository = new FileProductRepository();
        ProductService productService = new ProductService(repository);
        ProductController controller = new ProductController(productService, scanner);
        controller.startConsoleMenu();
        scanner.close();
    }
}
