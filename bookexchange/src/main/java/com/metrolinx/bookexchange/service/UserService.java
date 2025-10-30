package com.metrolinx.bookexchange.service;

import com.metrolinx.bookexchange.model.User;
import com.metrolinx.bookexchange.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExcelAuthService excelAuthService;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      ExcelAuthService excelAuthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.excelAuthService = excelAuthService;
    }
    
    public boolean isEmailAuthorized(String email) {
        return excelAuthService.isEmailAuthorized(email);
    }
    
    public User registerUser(String email, String password, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }
}