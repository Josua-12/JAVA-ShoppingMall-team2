package com.shopping.controller;

import com.shopping.model.Cart;
import com.shopping.model.Product;
import com.shopping.repository.FileCartRepository; // import는 그대로 유지
import com.shopping.repository.ProductRepository;

import java.util.Optional;
import java.util.Scanner;

public class CartController {

    private final FileCartRepository cartRepository; // 저장소 객체
    private final ProductRepository productRepository;
    private final Scanner scanner;

    //매개변수가 없는 기본 생성자로 변경
    public CartController() {
        this.cartRepository = new FileCartRepository();
        this.productRepository = productRepository;
        this.scanner = new Scanner(System.in);
    }

    /**
     * 장바구니 관리 메뉴를 시작하는 메인 메서드
     * @param userId 현재 로그인된 사용자의 ID
     */
    public void runCartMenu(String userId) {
        while (true) {
            System.out.println("\n--- 🛒 장바구니 관리 ---");
            System.out.println("1. 상품 추가");
            System.out.println("2. 장바구니 보기");
            System.out.println("3. 상품 삭제");
            System.out.println("4. 장바구니 비우기");
            System.out.println("0. 뒤로 가기");
            System.out.print("메뉴를 선택하세요: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                	System.out.print("추가할 상품 ID를 입력하세요 (예: prod-001): ");
                    String productId = scanner.nextLine();
                    
                    // 1. ProductRepository를 사용해 ID로 상품을 찾는다.
                    Optional<Product> productOptional = productRepository.findById(productId);

                    // 2. 상품이 존재하는지 확인한다.
                    if (productOptional.isPresent()) {
                        Product product = productOptional.get(); // Optional에서 실제 Product 객체를 꺼냄
                        
                        System.out.print("수량을 입력하세요: ");
                        int quantity = scanner.nextInt();
                        scanner.nextLine(); // 버퍼 비우기

                        // 3. 찾은 상품을 장바구니에 추가한다.
                        addProductToCart(userId, product, quantity);

                    } else {
                        // 상품이 존재하지 않는 경우
                        System.out.println("⚠️ 해당 ID의 상품을 찾을 수 없습니다.");
                    }
                    break;
                case 2:
                    viewCart(userId);
                    break;
                case 3:
                    System.out.print("삭제할 상품 ID를 입력하세요: ");
                    String productIdToRemove = scanner.nextLine();
                    removeProductFromCart(userId, productIdToRemove);
                    break;
                case 4:
                    clearCart(userId);
                    break;
                case 0:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }
    
    
    private void addProductToCart(String userId, Product product, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                                  .orElseGet(() -> new Cart(userId));
        
        cart.addProduct(product, quantity);
        cartRepository.save(cart);
        System.out.printf("✅ '%s' 상품 %d개를 장바구니에 추가했습니다.\n", product.getName(), quantity);
    }
    
    private void viewCart(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        
        if (cartOptional.isPresent() && !cartOptional.get().getItems().isEmpty()) {
            System.out.println(cartOptional.get());
        } else {
            System.out.println("🛒 장바구니가 비어있습니다.");
        }
    }
    
    private void removeProductFromCart(String userId, String productId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            if (cart.getItems().containsKey(productId)) {
                cart.removeProduct(productId);
                cartRepository.save(cart);
                System.out.println("✅ 상품을 장바구니에서 삭제했습니다.");
            } else {
                System.out.println("⚠️ 장바구니에 해당 상품이 없습니다.");
            }
        } else {
            System.out.println("🛒 장바구니가 비어있습니다.");
        }
    }
    
    private void clearCart(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (cartOptional.isPresent() && !cartOptional.get().getItems().isEmpty()) {
            Cart cart = cartOptional.get();
            cart.clear();
            cartRepository.save(cart);
            System.out.println("✅ 장바구니를 모두 비웠습니다.");
        } else {
            System.out.println("🛒 장바구니가 이미 비어있습니다.");
        }
    }
}