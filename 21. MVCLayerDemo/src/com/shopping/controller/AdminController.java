package com.shopping.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.shopping.model.Admin;
import com.shopping.model.Order;
import com.shopping.model.OrderItem;
import com.shopping.model.OrderStatus;
import com.shopping.model.Product;
import com.shopping.model.User;
import com.shopping.repository.UserRepository;
import com.shopping.service.OrderService;
import com.shopping.service.ProductService;
import com.shopping.service.UserService;

public class AdminController {
	private UserService userService;
	private OrderService orderService;
	private ProductService productService;
	private Scanner scanner;
    private UserRepository userRepository;
	
	public AdminController() {
		//super();
		this.userService = new UserService();
		this.orderService = new OrderService(null, null);
		this.productService = new ProductService(null);
		this.scanner = new Scanner(System.in);
	}
	
	
	
	// 관리자 기능 메뉴 표시 및 처리
	public void showAdminMenu() {
		while(true) {
			System.out.println("\n== 관리자 기능 ==");
			System.out.println("1. 상품 관리");
			System.out.println("2. 사용자 관리");
			System.out.println("3. 주문 관리");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				handleProductManagement();		// 상품 관리
				break;
			case "2":
				handleUserManagement();		// 사용자 관리
				break;
			case "3":
				handleOrderManagement();		// 주문 관리
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
	}



	private void handleOrderManagement() {
		while(true) {
			System.out.println("\n== 주문 관리 메뉴 ==");
			System.out.println("1. 전체 주문 목록 조회");
			System.out.println("2. 주문 상태 변경");
			System.out.println("3. 주문 통계 조회 (일별, 상품별)");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				displayAllOrders();		// 전체 주문 목록 조회
				break;
			case "2":
				updateOrderStatus();		// 주문 상태 변경
				break;
			case "3":
				displayOrderStatistics();		// 주문 통계 조회
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}
	
	 private void displayAllOrders() {
	        System.out.println("\n== 전체 주문 목록 ==");
	        List<Order> orders = orderService.getAllOrders();
	        if (orders.isEmpty()) {
	            System.out.println("주문 내역이 없습니다.");
	            return;
	        }
	        orders.forEach(System.out::println);
	    }
	 
	 private void updateOrderStatus() {
	        System.out.println("\n== 주문 상태 변경 ==");
	        System.out.print("상태를 변경할 주문의 ID를 입력하세요: ");
	        String orderId = scanner.nextLine();

	        Optional<Order> orderOpt = orderService.findOrderById(orderId);
	        if (orderOpt.isEmpty()) {
	            System.out.println("해당 ID의 주문을 찾을 수 없습니다.");
	            return;
	        }

	        Order order = orderOpt.get();
	        System.out.println("현재 주문 상태: " + order.getStatus().getDisplayName());

	        System.out.println("변경 가능한 상태 목록:");
	        for (OrderStatus status : OrderStatus.values()) {
	            if (order.getStatus().canTransitionTo(status)) {
	                System.out.printf("- %s (%s)\n", status.name(), status.getDisplayName());
	            }
	        }

	        System.out.print("변경할 상태를 입력하세요 (영문 대문자): ");
	        String nextStatusStr = scanner.nextLine().toUpperCase();

	        try {
	            OrderStatus nextStatus = OrderStatus.valueOf(nextStatusStr);
	            orderService.updateOrderStatus(orderId, nextStatus);
	            System.out.println("주문 상태가 성공적으로 변경되었습니다.");
	        } catch (IllegalArgumentException e) {
	            System.out.println("오류: 유효하지 않은 상태 값입니다.");
	        } catch (IllegalStateException e) {
	            System.out.println("상태 변경 오류: " + e.getMessage());
	        }
	    }
	 
	 private void displayOrderStatistics() {
	        System.out.println("\n== 주문 통계 조회 ==");
	        List<Order> orders = orderService.getAllOrders();
	        if (orders.isEmpty()) {
	            System.out.println("분석할 주문 데이터가 없습니다.");
	            return;
	        }

	        // 1. 일별 매출 통계
	        Map<LocalDate, Double> dailySales = orders.stream()
	                .collect(Collectors.groupingBy(
	                        order -> order.getOrderDate().toLocalDate(),
	                        Collectors.summingDouble(Order::getTotalPrice)
	                ));

	        System.out.println("\n--- 일별 총 매출 ---");
	        dailySales.entrySet().stream()
	                .sorted(Map.Entry.comparingByKey())
	                .forEach(entry -> System.out.printf("%s: %,.0f원\n", entry.getKey(), entry.getValue()));

	        // 2. 상품별 판매 통계
	        Map<String, int[]> productSales = orders.stream()
	                .flatMap(order -> order.getItems().stream())
	                .collect(Collectors.groupingBy(
	                        OrderItem::getProductId,
	                        Collectors.reducing(new int[2],
	                                item -> new int[]{item.getQuantity(), item.getLineTotal()},
	                                (a, b) -> new int[]{a[0] + b[0], a[1] + b[1]})
	                ));

	        System.out.println("\n--- 상품별 판매 현황 (판매량, 총 매출) ---");
	        productSales.forEach((productId, data) -> {
	            String productName = productService.getProductById(productId)
	                                                 .map(Product::getName)
	                                                 .orElse("알 수 없는 상품");
	            System.out.printf("%s (%s): %d개, %,d원\n", productName, productId, data[0], data[1]);
	        });
	    }
	 



	private void handleUserManagement() {
		while(true) {
			System.out.println("\n== 사용자 관리 메뉴 ==");
			System.out.println("1. 전체 회원 목록 조회");
			System.out.println("2. 회원 검색 (ID, 이름, 이메일)");
			System.out.println("3. 회원 상세 정보 조회");
			System.out.println("4. 회원 강제 탈퇴");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				displayAllUsers();		// 전체 회원 목록 조회
				break;
			case "2":
				searchUsers();		// 회원 검색 (ID, 이름, 이메일)
				break;
			case "3":
				displayUserDetails();		// 회원 상세 정보 조회
				break;
			case "4":
				deactivateUser();		// 회원 강제 탈퇴
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}

	
	private void deactivateUser() {
        System.out.println("\n== 회원 강제 탈퇴 ==");
        System.out.print("탈퇴시킬 회원의 ID를 입력하세요: ");
        String id = scanner.nextLine().trim();
        if (userService.deleteUser(id)) {
            System.out.println("회원이 성공적으로 탈퇴 처리되었습니다.");
        } else {
            System.out.println("해당 ID의 회원을 찾을 수 없거나 탈퇴 처리에 실패했습니다.");
        }
    }


	private void searchUsers() {
		while(true) {
			System.out.println("\n== 회원 검색 ==");
			System.out.println("1. ID로 회원 검색");
			System.out.println("2. 이름으로 회원 검색");
			System.out.println("3. 이메일로 회원 검색");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				searchById();		// ID로 회원 검색
				break;
			case "2":
				searchByName();		// 이름으로 회원 검색
				break;
			case "3":
				searchByEmail();		// 이메일로 회원 검색
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}

	private void searchUsersByName() {
        System.out.print("검색할 회원 이름의 일부를 입력하세요: ");
        String name = scanner.nextLine().trim();
        List<User> users = userService.findByName(name);
        if (users.isEmpty()) {
            System.out.println("해당 이름의 회원을 찾을 수 없습니다.");
        } else {
            System.out.println("검색 결과:");
            users.forEach(System.out::println);
        }
    }

	private void searchUsersById() {
		System.out.print("검색할 회원의 ID를 입력하세요: ");
        String id = scanner.nextLine().trim();
        Optional<User> user = Optional.ofNullable(userService.findById(id));
        if (user.isPresent()) {
            System.out.println("검색 결과:\n" + user.get());
        } else {
            System.out.println("해당 ID의 회원을 찾을 수 없습니다.");
        }
		
	}

	 private void searchUsersByEmail() {
	        System.out.print("검색할 회원의 이메일을 입력하세요: ");
	        String email = scanner.nextLine().trim();
	        Optional<User> user = Optional.ofNullable(userService.findByEmail(email));
	        if (user.isPresent()) {
	            System.out.println("검색 결과:\n" + user.get());
	        } else {
	            System.out.println("해당 이메일의 회원을 찾을 수 없습니다.");
	        }
	    }

	private void displayAllUsers() {
		System.out.println("\n== 전체 사용자 목록 ==");
        List<User> users = userService.findAll();
        if (users.isEmpty()) {
            System.out.println("등록된 사용자가 없습니다.");
            return;
        }
        users.forEach(System.out::println);
	}
	
	private void displayUserDetails() {
        System.out.println("\n== 회원 상세 정보 조회 ==");
        System.out.print("조회할 회원의 ID를 입력하세요: ");
        String id = scanner.nextLine().trim();
        Optional<User> userOpt = Optional.ofNullable(userService.findById(id));

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("--- 회원 정보 ---");
            System.out.println("ID: " + user.getId());
            System.out.println("이름: " + user.getName());
            System.out.println("이메일: " + user.getEmail());
            System.out.println("역할: " + user.getRole());
            System.out.printf("잔액: %.2f\n", user.getBalance());
        } else {
            System.out.println("해당 ID의 회원을 찾을 수 없습니다.");
        }
    }



	private void handleProductManagement() {
		while(true) {
			System.out.println("\n== 상품 관리 메뉴 ==");
			System.out.println("1. 신규 상품 등록");
			System.out.println("2. 상품 정보 수정");
			System.out.println("3. 상품 삭제");
			System.out.println("4. 재고 관리 (입고 처리)");
			System.out.println("0. 돌아가기");
			System.out.print("선택: ");
			
			String choice = scanner.nextLine();
			
			// 사용자 선택에 따른 메소드 호출
			switch(choice) {
			case "1":
				addNewProduct();		// 신규 상품 등록
				break;
			case "2":
				updateProductInfo();		// 상품 정보 수정
				break;
			case "3":
				deleteProduct();		// 상품 삭제
				break;
			case "4":
				manageProductStock();		// 재고 관리 (입고 처리)
				break;
			case "0":
				return;			// 메인 메뉴로 돌아가기
			default:
				System.out.println("잘못된 선택입니다.");
			}
		}
		
	}
	 private void addNewProduct() {
	        try {
	            System.out.println("\n== 신규 상품 등록 ==");
	            System.out.print("상품 ID: ");
	            String id = scanner.nextLine();
	            System.out.print("상품명: ");
	            String name = scanner.nextLine();
	            System.out.print("가격: ");
	            double price = Double.parseDouble(scanner.nextLine());
	            System.out.print("재고: ");
	            int stock = Integer.parseInt(scanner.nextLine());
	            System.out.print("카테고리: ");
	            String category = scanner.nextLine();

	            Product newProduct = new Product(id, name, price, stock, category);
	            productService.addProduct(name, price, stock);		//addproduct가 저장소에 저장되는 방식이 아니라 생성자로 생성하는 방식?
	            System.out.println("상품이 성공적으로 등록되었습니다.");
	        } catch (NumberFormatException e) {
	            System.out.println("오류: 가격과 재고는 숫자로 입력해야 합니다.");
	        } catch (Exception e) {
	            System.out.println("상품 등록 중 오류 발생: " + e.getMessage());
	        }
	    }

	    private void updateProductInfo() {
	        System.out.println("\n== 상품 정보 수정 ==");
	        System.out.print("수정할 상품의 ID를 입력하세요: ");
	        String id = scanner.nextLine();

	        Optional<Product> optionalProduct = productService.getProductById(id);
	        if (optionalProduct.isEmpty()) {
	            System.out.println("해당 ID의 상품을 찾을 수 없습니다.");
	            return;
	        }

	        Product product = optionalProduct.get();
	        System.out.println("기존 정보: " + product);

	        try {
	            System.out.print("새 상품명 (변경 없으면 Enter): ");
	            String name = scanner.nextLine();
	            if (!name.isBlank()) product.setName(name);

	            System.out.print("새 가격 (변경 없으면 Enter): ");
	            String priceStr = scanner.nextLine();
	            if (!priceStr.isBlank()) product.setPrice(Double.parseDouble(priceStr));

	            System.out.print("새 재고 (변경 없으면 Enter): ");
	            String stockStr = scanner.nextLine();
	            if (!stockStr.isBlank()) product.setStock(Integer.parseInt(stockStr));

	            System.out.print("새 카테고리 (변경 없으면 Enter): ");
	            String category = scanner.nextLine();
	            if (!category.isBlank()) product.setCategory(category);

	            productService.updateProduct(product);
	            System.out.println("상품 정보가 성공적으로 수정되었습니다.");
	        } catch (NumberFormatException e) {
	            System.out.println("오류: 가격과 재고는 숫자로 입력해야 합니다.");
	        } catch (Exception e) {
	            System.out.println("수정 중 오류 발생: " + e.getMessage());
	        }
	    }

	    private void deleteProduct() {
	        System.out.println("\n== 상품 삭제 ==");
	        System.out.print("삭제할 상품의 ID를 입력하세요: ");
	        String id = scanner.nextLine();

	        if (productService.deleteProduct(id)) {
	            System.out.println("상품이 성공적으로 삭제되었습니다.");
	        } else {
	            System.out.println("해당 ID의 상품을 찾을 수 없거나 삭제에 실패했습니다.");
	        }
	    }

	    private void manageProductStock() {
	        System.out.println("\n== 재고 관리 (입고) ==");
	        System.out.print("재고를 추가할 상품의 ID를 입력하세요: ");
	        String id = scanner.nextLine();

	        Optional<Product> optionalProduct = productService.getProductById(id);
	        if (optionalProduct.isEmpty()) {
	            System.out.println("해당 ID의 상품을 찾을 수 없습니다.");
	            return;
	        }

	        try {
	            System.out.print("추가할 재고 수량을 입력하세요: ");
	            int quantity = Integer.parseInt(scanner.nextLine());
	            if (quantity <= 0) {
	                System.out.println("입고 수량은 0보다 커야 합니다.");
	                return;
	            }
	            productService.addStock(id, quantity);
	            System.out.println("재고가 성공적으로 추가되었습니다. 현재 재고: " + productService.getProductById(id).get().getStock());
	        } catch (NumberFormatException e) {
	            System.out.println("오류: 수량은 숫자로 입력해야 합니다.");
	        } catch (Exception e) {
	            System.out.println("재고 추가 중 오류 발생: " + e.getMessage());
	        }
	    }

}
