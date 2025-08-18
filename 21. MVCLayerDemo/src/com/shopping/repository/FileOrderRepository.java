package com.shopping.repository;

import com.shopping.model.Order;
import com.shopping.model.OrderStatus;
import com.shopping.repository.OrderRepository;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FileOrderRepository
 * --------------------
 * OrderRepository 인터페이스를 파일 기반으로 구현한 클래스.
 * - 주문 데이터를 자바 직렬화 방식으로 파일에 저장/조회/삭제/갱신
 * - 애플리케이션이 꺼져도 주문 정보가 유지됨(영속성 보장)
 * - 내부적으로 Map<String, Order> 형태로 캐시를 두고 관리
 */
public abstract class FileOrderRepository implements OrderRepository {

    private final Path storePath;   // 실제 주문 데이터가 저장될 파일 경로 (예: data/orders.dat)
    private final Path tempPath;    // 임시 파일 경로 (쓰기 도중 실패 시 안전성 확보용)
    private Map<String, Order> cache; // 메모리 캐시: 프로그램 기동 시 파일에서 로딩됨

    /**
     * 생성자: 파일 경로를 받아 초기화
     * - 파일이 없으면 빈 맵을 직렬화해서 새로 생성
     * - 기존 파일이 있으면 loadAll()로 로드
     */
    public FileOrderRepository(String filePath) {
        this.storePath = Paths.get(filePath);
        this.tempPath = Paths.get(filePath + ".tmp");
        ensureFile();       // 파일 존재 보장
        this.cache = loadAll(); // 캐시에 전체 주문 로드
    }

    // ---------- 인터페이스 구현 ----------

    /**
     * 주문 저장 (Create/Update)
     * - orderId가 없으면 nextId()로 새 ID 부여
     * - 캐시에 put() 후, 전체를 파일에 직렬화 저장
     */
    @Override
    public synchronized void save(Order order) {
        if (order == null) throw new IllegalArgumentException("order is null");

        // ID 자동 발급
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            order.setOrderId(nextId());
        }
        cache.put(order.getOrderId(), order);
        persist(cache);
    }

    /**
     * 주문 단건 조회 (Read by ID)
     */
    @Override
    public synchronized Optional<Order> findById(String orderId) {
        return Optional.ofNullable(cache.get(orderId));
    }

    /**
     * 모든 주문 조회 (Read all)
     */
    @Override
    public synchronized List<Order> findAll() {
        return new ArrayList<>(cache.values());
    }

    // 페이징 지원 버전 (현재는 주석 처리)
    // 인터페이스에 메서드가 없으면 @Override 제거 후 구현체 전용으로 둘 수 있음
//    @Override
//    public synchronized List<Order> findAll(int page, int size) {
//        if (page < 0 || size <= 0) throw new IllegalArgumentException("invalid page/size");
//        List<Order> all = new ArrayList<>(cache.values());
//        int from = page * size;
//        if (from >= all.size()) return Collections.emptyList();
//        int to = Math.min(from + size, all.size());
//        return all.subList(from, to);
//    }

    /**
     * 주문 삭제 (Delete)
     */
    @Override
    public synchronized boolean delete(String orderId) {
        if (cache.remove(orderId) != null) {
            persist(cache);
            return true;
        }
        return false;
    }

    /**
     * 주문 상태 업데이트 (Update status only)
     * - 상태 전이(valid transition)는 Order.changeStatus()에서 검증
     */
    public synchronized boolean updateStatus(String orderId, OrderStatus newStatus) {
        Order o = cache.get(orderId);
        if (o == null) return false;
        // 상태 전이 검증은 Order.changeStatus가 수행
        o.changeStatus(newStatus);
        persist(cache);
        return true;
    }

    /**
     * 특정 고객 ID의 모든 주문 조회
     */
    @Override
    public synchronized List<Order> findByUserId(String userId) {
        return cache.values().stream()
                .filter(o -> Objects.equals(o.getUserId(), userId))
                .collect(Collectors.toList());
    }

    /**
     * 특정 상태(Status)에 해당하는 모든 주문 조회
     */
    public synchronized List<Order> findByStatus(OrderStatus status) {
        return cache.values().stream()
                .filter(o -> o.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * 날짜 범위로 주문 조회
     */
    @Override
    public synchronized List<Order> findByDateRange(LocalDate from, LocalDate to) {
        return cache.values().stream()
                .filter(o -> {
                    LocalDate d = o.getOrderDate().toLocalDate();
                    return (from == null || !d.isBefore(from))
                        && (to == null   || !d.isAfter(to));
                })
                .collect(Collectors.toList());
    }

    /**
     * 주문 ID 자동 생성기
     * - 현재 캐시에 있는 주문 ID 중 가장 큰 번호 찾아서 +1
     * - 결과: O1, O2, O3 ...
     */
    @Override
    public synchronized String nextId() {
        long max = cache.keySet().stream()
                .filter(k -> k.startsWith("O"))   // O로 시작하는 것만
                .map(k -> k.substring(1))        // 숫자 부분만 추출
                .filter(s -> s.matches("\\d+"))  // 숫자인 경우만
                .mapToLong(Long::parseLong)
                .max().orElse(0L);
        return "O" + (max + 1);
    }

    // ---------- 내부 유틸 ----------

    /**
     * 저장 파일 존재 보장
     * - 없으면 빈 맵을 직렬화해서 새로 생성
     */
    private void ensureFile() {
        try {
            if (storePath.getParent() != null) Files.createDirectories(storePath.getParent());
            if (Files.notExists(storePath)) persist(new HashMap<>());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 파일에서 Map<String, Order> 전체를 역직렬화해서 캐시에 로드
     */
    @SuppressWarnings("unchecked")
    private Map<String, Order> loadAll() {
        if (Files.notExists(storePath)) return new HashMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(storePath))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) return (Map<String, Order>) obj;
            return new HashMap<>();
        } catch (EOFException e) { // 파일은 있으나 내용이 비어 있을 때
            return new HashMap<>();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load orders: " + storePath, e);
        }
    }

    /**
     * 캐시에 담긴 주문 전체를 파일에 직렬화하여 저장
     * - 임시 파일에 먼저 기록 후 원본으로 교체 → 원자적 저장(atomic move)
     */
    private void persist(Map<String, Order> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                tempPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(data);
            oos.flush();
            try {
                // 원자적 이동 지원 시 → 깨끗하게 교체
                Files.move(tempPath, storePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // 파일 시스템이 ATOMIC_MOVE 지원 안하면 그냥 교체
                Files.move(tempPath, storePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}