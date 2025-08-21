package com.shopping.controller;

import com.shopping.model.Product;
import com.shopping.model.ProductCategory;
import com.shopping.repository.FileProductRepository;
import com.shopping.service.ProductService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * 상품 관련 사용자 요청을 처리하고 ProductService와 통신하여 비즈니스 로직을 실행하는 컨트롤러 클래스입니다.
 * 사용자 메뉴 인터페이스(CLI)를 제공합니다.
 */
public class ProductController {

    private final ProductService productService;
    private final Scanner scanner;
    private static final int PAGE_SIZE = 10; // 페이지당 표시할 상품 수

    public ProductController(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
    }

    /**
     * 애플리케이션의 메인 메뉴를 시작합니다.
     */
    public void startMainMenu() {
    	startProductSearchMenu();
    }

    /**
     * 일반 사용자를 위한 상품 조회 메뉴를 표시합니다.
     */
    public void startProductSearchMenu() {
        int choice;
        do {
            System.out.println("\n--- 상품 조회 메뉴 ---");
            System.out.println("1. 전체 상품 목록 (페이징)");
            System.out.println("2. 카테고리별 상품 조회");
            System.out.println("3. 가격대별 상품 조회");
            System.out.println("4. 베스트셀러 보기");
            System.out.println("5. 신상품 보기");
            System.out.println("6. 상품 상세 정보 조회");
            System.out.println("0. 메인 메뉴로 돌아가기");
            System.out.print("메뉴 선택: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1: listAllProductsPaginated(); break;
                    case 2: searchByCategory(); break;
                    case 3: searchByPriceRange(); break;
                    case 4: listBestSellers(); break;
                    case 5: listNewArrivals(); break;
                    case 6: viewProductDetail(); break;
                    case 0: System.out.println("메인 메뉴로 돌아갑니다."); break;
                    default: System.out.println("잘못된 메뉴 선택입니다.");
                }
            } catch (NumberFormatException e) {
                System.out.println("오류: 유효한 숫자 메뉴를 입력하세요.");
                choice = -1;
            }
        } while (choice != 0);
    }


    /**
     * 전체 상품 목록을 페이지 단위로 나누어 보여줍니다.
     */
    private void listAllProductsPaginated() {
        int page = 1;
        long totalProducts = productService.getTotalProductCount();
        long totalPages = (long) Math.ceil((double) totalProducts / PAGE_SIZE);

        if (totalProducts == 0) {
            System.out.println("등록된 상품이 없습니다.");
            return;
        }

        while (true) {
            System.out.printf("\n--- 전체 상품 목록 (페이지 %d / %d) ---\n", page, totalPages);
            List<Product> products = productService.getAllProducts(page, PAGE_SIZE);
            printProductList(products);

            System.out.print("이동할 페이지 입력 (다음: n, 이전: p, 종료: e): ");
            String command = scanner.nextLine();

            if ("n".equalsIgnoreCase(command)) {
                if (page < totalPages) page++;
                else System.out.println("마지막 페이지입니다.");
            } else if ("p".equalsIgnoreCase(command)) {
                if (page > 1) page--;
                else System.out.println("첫 페이지입니다.");
            } else if ("e".equalsIgnoreCase(command)) {
                break;
            } else {
                try {
                    int pageNum = Integer.parseInt(command);
                    if (pageNum > 0 && pageNum <= totalPages) {
                        page = pageNum;
                    } else {
                        System.out.println("유효하지 않은 페이지 번호입니다.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("잘못된 입력입니다. n, p, e 또는 페이지 번호를 입력하세요.");
                }
            }
        }
    }

    private void listBestSellers() {
        System.out.println("\n--- 베스트셀러 TOP 5 ---");
        List<Product> products = productService.getBestSellers(5);
        printProductList(products);
    }

    private void listNewArrivals() {
        System.out.println("\n--- 신상품 TOP 5 ---");
        List<Product> products = productService.getNewArrivals(5);
        printProductList(products);
    }

    private void searchByCategory() {
        System.out.printf("조회할 카테고리를 입력하세요 (%s): ", ProductCategory.getCategoryNames());
        String category = scanner.nextLine();
        List<Product> products = productService.findProductsByCategory(category);
        System.out.printf("\n--- 카테고리 '%s' 검색 결과 ---\n", category);
        printProductList(products);
    }

    private void searchByPriceRange() {
        System.out.println("\n--- 가격대 선택 ---");
        System.out.println("1. 1만원 미만");
        System.out.println("2. 1만원 ~ 5만원 미만");
        System.out.println("3. 5만원 ~ 10만원 미만");
        System.out.println("4. 10만원 이상");
        System.out.print("선택: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            int min = 0, max = -1; // max=-1은 무한대를 의미
            String rangeStr = "";

            switch (choice) {
                case 1: min = 0; max = 10000; rangeStr = "1만원 미만"; break;
                case 2: min = 10000; max = 50000; rangeStr = "1만원 ~ 5만원"; break;
                case 3: min = 50000; max = 100000; rangeStr = "5만원 ~ 10만원"; break;
                case 4: min = 100000; max = -1; rangeStr = "10만원 이상"; break;
                default: System.out.println("잘못된 선택입니다."); return;
            }
            List<Product> products = productService.findProductsByPriceRange(min, max);
            System.out.printf("\n--- 가격대 '%s' 검색 결과 ---\n", rangeStr);
            printProductList(products);
        } catch (NumberFormatException e) {
            System.out.println("오류: 숫자를 입력하세요.");
        }
    }

    private void searchByName() {
        System.out.print("검색할 상품명을 입력하세요: ");
        String name = scanner.nextLine();
        List<Product> products = productService.findProductsByName(name);
        System.out.printf("\n--- 상품명 '%s' 검색 결과 ---\n", name);
        printProductList(products);
    }

    public void viewProductDetail() {
        System.out.print("\n조회할 상품의 ID를 입력하세요: ");
        String id = scanner.nextLine();

        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            System.out.println("\n--- 상품 정보 ---");
            System.out.println("ID: " + product.getId());
            System.out.println("이름: " + product.getName());
            System.out.println("카테고리: " + product.getCategory().name());
            System.out.println("가격: " + String.format("%,.0f원", product.getPrice()));
            System.out.println("재고: " + product.getStock() + "개");
            System.out.println("상품 설명: " + product.getDescription());
            System.out.println("등록일시: " + product.getRegistrationDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("누적 판매량: " + product.getSalesCount());
        } else {
            System.out.println("해당 ID의 상품이 존재하지 않습니다.");
        }
    }

    public void addProduct() {
        System.out.println("\n--- 새 상품 등록 ---");
        try {
        	System.out.print("상품 ID: ");
            String id = scanner.nextLine();
            System.out.print("상품 이름: ");
            String name = scanner.nextLine();
            System.out.printf("상품 카테고리 (%s): ", ProductCategory.getCategoryNames());
            String categoryInput = scanner.nextLine();
            ProductCategory category = ProductCategory.fromString(categoryInput);
            System.out.print("상품 가격: ");
            int price = Integer.parseInt(scanner.nextLine());
            System.out.print("초기 재고: ");
            int stock = Integer.parseInt(scanner.nextLine());
            System.out.print("상품 설명 (500자 이내): ");
            String description = scanner.nextLine();

            Product product = new Product(id, name, category, price, stock, description);
            Product newProduct = productService.addProduct(product);
            System.out.println("상품 '" + newProduct.getName() + "' (ID: " + newProduct.getId() + ") 이(가) 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("입력 오류: " + e.getMessage());
        }
    }

    public void updateProduct() {
        System.out.print("\n수정할 상품의 ID를 입력하세요: ");
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
            if (!priceInput.isEmpty()) currentProduct.setPrice(Integer.parseInt(priceInput));

            productService.updateProduct(currentProduct);
            System.out.println("ID " + id + " 상품 정보가 수정되었습니다.");
        } 
        /*
         * [수정] 컴파일 오류 해결을 위해 multi-catch 블록을 분리했습니다.
         * NumberFormatException은 숫자가 아닌 값을 입력했을 때,
         * IllegalArgumentException은 Service의 유효성 검증 실패 시 발생합니다.
         */
        catch (NumberFormatException e) {
            System.out.println("오류: 가격은 숫자로 입력해야 합니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    public void deleteProduct() {
        System.out.print("\n삭제할 상품의 ID를 입력하세요: ");
        String id = scanner.nextLine();
        try {
            if (productService.deleteProduct(id)) {
                System.out.println("ID " + id + " 상품이 삭제되었습니다.");
            } else {
                System.out.println("해당 ID의 상품이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    public void changeProductStock() {
        try {
            System.out.print("\n재고를 추가할 상품 ID: ");
            String id = scanner.nextLine();
            System.out.print("추가할 수량 입력: ");
            int amount = Integer.parseInt(scanner.nextLine());
            productService.addStock(id, amount);
            productService.findProductById(id).ifPresent(p ->
                    System.out.println("ID " + id + " 상품 재고가 변경되었습니다. (현재 재고: " + p.getStock() + ")")
            );
        }
        /*
         * [수정] 컴파일 오류 해결을 위해 multi-catch 블록을 분리했습니다.
         * NumberFormatException은 숫자가 아닌 값을 입력했을 때,
         * IllegalArgumentException은 Service의 유효성 검증 실패 시 발생합니다.
         */
        catch (NumberFormatException e) {
            System.out.println("오류: 수량은 숫자로 입력해야 합니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("오류: " + e.getMessage());
        }
    }

    /**
     * 상품 목록을 정해진 형식에 맞춰 콘솔에 출력하는 헬퍼 메소드입니다.
     * @param products 출력할 상품 리스트
     */
    private void printProductList(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("표시할 상품이 없습니다.");
        } else {
            System.out.printf("%-10s %-20s %-12s %-5s %-10s\n", "ID", "이름", "가격", "재고", "카테고리");
            System.out.println("-------------------------------------------------------------------");
            for (Product product : products) {
                System.out.printf("%-10s %-20s %-12.0f %-5d %-10s\n",
                        product.getId(), product.getName(), product.getPrice(),
                        product.getStock(), product.getCategory().name());
            }
        }
    }

    /**
     * 애플리케이션 실행 진입점(Entry Point)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FileProductRepository repository = new FileProductRepository();
        ProductService productService = new ProductService(repository);
        ProductController controller = new ProductController(productService, scanner);
        controller.startMainMenu();
        scanner.close();
    }
}