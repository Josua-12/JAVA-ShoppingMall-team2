package com.shopping.service;

import com.shopping.repository.UserRepository;

public class AdminService {

	private final UserRepository userRepository;
	
	public AdminService() {
        this.userRepository = new UserRepository();
    }
	
	public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
