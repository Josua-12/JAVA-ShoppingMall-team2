package com.shopping.controller;

import com.shopping.model.Product; // Assuming Product model exists
import com.shopping.service.ProductService;

import java.util.List;
import java.util.InputMismatchException;
import java.util.Scanner; // For console input in this example

/**
 * 상품 관련 요청을 처리하고 ProductService와 통신하여 비즈니스 로직을 실행하는 컨트롤러 클래스입니다.
 * 사용자의 입력을 받고, Service 계층의 메서드를 호출하며, 결과를 사용자에게 반환합니다.
 * 역할: 상품 목록 조회, 상세 보기, 상품 등록/수정/삭제, 재고 변경 등 상품 기능의 흐름 제어.
 * 설계 포인트:
 * 1) 필드: ProductService 참조
 * 2) 메서드: listProducts(), viewProductDetail(), addProduct(), updateProduct(), deleteProduct(),
 * (재고 변경 기능 포함)
 */
public class ProductController {

    // 설계 포인트 1: ProductService 참조
    private final ProductService productService;
    private final Scanner scanner; // 콘솔 입력을 위한 Scanner (실제 웹 애플리케이션에서는 사용되지 않음)

    /*
     * ProductController의 생성자입니다. ProductService 인스턴스를 주입받습니다.
     * @param productService 상품 관련 비즈니스 로직을 처리하는 서비스 객체
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
        this.scanner = new Scanner(System.in);
    }

    /*
     * 설계 포인트 2: listProducts()
     * 모든 상품 목록을 조회하고 콘솔에 출력합니다.
     */
    public void listProducts() {
        System.out.println("\n--- 상품 목록 ---");
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("등록된 상품이 없습니다.");
        } else {
            System.out.printf("%-5s %-20s %-10s %-5s %-10s\n", "ID", "이름", "가격", "재고", "카테고리");
            System.out.println("----------------------------------------------------------");
            for (Product product : products) {
                System.out.printf("%-5d %-20s %-10.0f %-5d %-10s\n",
                                  product.getId(),
                                  product.getName(),
                                  product.getPrice(),
                                  product.getStock(),
                                  product.getCategory());
            }
        }
    }

    /*
     * 설계 포인트 2: viewProductDetail()
     * 사용자로부터 상품 ID를 입력받아 해당 상품의 상세 정보를 조회하고 출력합니다.
     */
    public void viewProductDetail() {
        System.out.println("\n--- 상품 상세 조회 ---");
        System.out.print("조회할 상품의 ID를 입력하세요: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

            Product product = productService.findProductById(id);
            System.out.println("\n--- 상품 정보 ---");
            System.out.println("ID: " + product.getId());
            System.out.println("이름: " + product.getName());
            System.out.println("가격: " + String.format("%,.0f원", product.getPrice())); // 가격 포맷팅
            System.out.println("재고: " + product.getStock() + "개");
            System.out.println("카테고리: " + product.getCategory());

        } catch (InputMismatchException e) {
            System.out.println("오류: 유효한 숫자(ID)를 입력하세요.");
            scanner.nextLine(); // 잘못된 입력 소비
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /*
     * 설계 포인트 2: addProduct()
     * 사용자로부터 새 상품 정보를 입력받아 상품을 등록합니다.
     */
    public void addProduct() {
        System.out.println("\n--- 새 상품 등록 ---");
        try {
            System.out.print("상품 이름: ");
            String name = scanner.nextLine();
            System.out.print("상품 가격: ");
            double price = scanner.nextDouble();
            System.out.print("초기 재고: ");
            int stock = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

            // ProductService의 addProduct (카테고리 기본값 사용) 호출
            Product newProduct = productService.addProduct(name, price, stock);
            System.out.println("상품 '" + newProduct.getName() + "' (ID: " + newProduct.getId() + ") 이(가) 성공적으로 등록되었습니다.");
        } catch (InputMismatchException e) {
            System.out.println("오류: 가격과 재고는 숫자로 입력하세요.");
            scanner.nextLine(); // 잘못된 입력 소비
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /*
     * 설계 포인트 2: updateProduct()
     * 사용자로부터 상품 ID, 새 이름, 새 가격을 입력받아 기존 상품의 정보를 수정합니다.
     */
    public void updateProduct() {
        System.out.println("\n--- 상품 정보 수정 ---");
        try {
            System.out.print("수정할 상품의 ID를 입력하세요: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

            // 현재 상품 정보 미리 보여주기
            Product currentProduct = productService.findProductById(id);
            System.out.print("새 상품 이름 (현재: " + currentProduct.getName() + ", 변경 없으면 엔터): ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) { // 입력이 없으면 기존 이름 유지
                newName = currentProduct.getName();
            }

            System.out.print("새 상품 가격 (현재: " + String.format("%,.0f", currentProduct.getPrice()) + ", 변경 없으면 0 입력 후 엔터): ");
            double newPrice = scanner.nextDouble();
            scanner.nextLine(); // 개행 문자 소비
            if (newPrice == 0) { // 0 입력 시 기존 가격 유지 (0은 유효성 검사에서 걸림, 실제로는 -1 같은 특별 값 사용 권장)
                                  // 여기서는 예시를 위해 0으로 간주하지만, 실제로는 더 견고한 처리가 필요
                newPrice = currentProduct.getPrice();
            }


            productService.updateProduct(id, newName, newPrice);
            System.out.println("ID " + id + " 상품 정보가 성공적으로 수정되었습니다.");
        } catch (InputMismatchException e) {
            System.out.println("오류: ID와 가격은 숫자로 입력하세요.");
            scanner.nextLine(); // 잘못된 입력 소비
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /*
     * 설계 포인트 2: deleteProduct()
     * 사용자로부터 상품 ID를 입력받아 해당 상품을 삭제합니다.
     */
    public void deleteProduct() {
        System.out.println("\n--- 상품 삭제 ---");
        System.out.print("삭제할 상품의 ID를 입력하세요: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

            productService.deleteProduct(id);
            System.out.println("ID " + id + " 상품이 성공적으로 삭제되었습니다.");
        } catch (InputMismatchException e) {
            System.out.println("오류: 유효한 숫자(ID)를 입력하세요.");
            scanner.nextLine(); // 잘못된 입력 소비
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /*
     * 재고 변경 기능 (관리자 권한으로 간주).
     * 사용자로부터 상품 ID와 변경할 수량(양수: 증가, 음수: 감소)을 입력받아 재고를 변경합니다.
     */
    public void changeProductStock() {
        System.out.println("\n--- 상품 재고 변경 (관리자) ---");
        try {
            System.out.print("재고를 변경할 상품의 ID를 입력하세요: ");
            int id = scanner.nextInt();
            System.out.print("변경할 수량 (양수: 증가, 음수: 감소): ");
            int amount = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

            productService.changeStock(id, amount);
            Product product = productService.findProductById(id); // 변경된 재고 확인을 위해 다시 조회
            System.out.println("ID " + id + " 상품의 재고가 성공적으로 변경되었습니다. (현재 재고: " + product.getStock() + ")");
        } catch (InputMismatchException e) {
            System.out.println("오류: ID와 수량은 숫자로 입력하세요.");
            scanner.nextLine(); // 잘못된 입력 소비
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /*
     * 콘솔 기반의 간단한 메뉴를 표시하고 사용자 입력을 처리합니다.
     * 이 메서드는 {@code main} 메서드에서 호출되어 프로그램의 진입점 역할을 할 수 있습니다.
     */
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
            System.out.print("메뉴를 선택하세요: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // 개행 문자 소비

                switch (choice) {
                    case 1:
                        listProducts();
                        break;
                    case 2:
                        viewProductDetail();
                        break;
                    case 3:
                        addProduct();
                        break;
                    case 4:
                        updateProduct();
                        break;
                    case 5:
                        deleteProduct();
                        break;
                    case 6:
                        changeProductStock();
                        break;
                    case 0:
                        System.out.println("상품 관리 시스템을 종료합니다.");
                        break;
                    default:
                        System.out.println("잘못된 메뉴 선택입니다. 0-6 사이의 숫자를 입력하세요.");
                }
            } catch (InputMismatchException e) {
                System.out.println("오류: 유효한 숫자 메뉴를 입력하세요.");
                scanner.nextLine(); // 잘못된 입력 소비
                choice = -1; // 루프를 계속 돌리기 위해 유효하지 않은 값으로 설정
            }
        } while (choice != 0);

        scanner.close(); // 프로그램 종료 시 Scanner 리소스 해제
    }

    /*
     * 애플리케이션의 메인 진입점입니다.
     * ProductService와 ProductController를 초기화하고 콘솔 메뉴를 시작합니다.
     */
    public static void main(String[] args) {
        ProductService productService = new ProductService(); // ProductService 인스턴스 생성 (의존성)
        ProductController productController = new ProductController(productService); // ProductService를 ProductController에 주입

        productController.startConsoleMenu(); // 콘솔 메뉴 시작
    }
}
