package com.shopping.repository;

import java.util.List;
import com.shopping.model.Admin;
import com.shopping.persistence.FileManager;
import com.shopping.util.Constants;

public class AdminRepository {

    private static final String FILE_NAME = Constants.ADMIN_DATA_FILE;

    public Admin save(Admin admin) {
        List<Admin> admins = FileManager.readFromFile(FILE_NAME);
        admins.add(admin);
        FileManager.writeToFile(FILE_NAME, admins);
        return admin;
    }

    public Admin findByEmail(String email) {
        List<Admin> admins = FileManager.readFromFile(FILE_NAME);
        return admins.stream()
                .filter(a -> a.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }
}
