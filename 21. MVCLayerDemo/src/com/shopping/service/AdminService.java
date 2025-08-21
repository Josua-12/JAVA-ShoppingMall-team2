balancepackage com.shopping.service;

import com.shopping.repository.FileUserRepository;
import com.shopping.repository.UserRepository;

public class AdminService {

    private final UserRepository userRepository;

    // 기본 생성자: FileUserRepository 사용
    public AdminService() {
        this.userRepository = new FileUserRepository();
    }

    // 외부에서 UserRepository 주입 가능
    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 예: 회원 강제 탈퇴 기능
    public void deactivateUser(String userId) {
        if (userRepository.findById(userId) != null) {
            userRepository.deleteById(userId);
        } else {
            throw new IllegalArgumentException("해당 ID의 회원이 존재하지 않습니다.");
        }
    }
}
