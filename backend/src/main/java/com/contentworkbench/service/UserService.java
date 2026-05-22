package com.contentworkbench.service;

import com.contentworkbench.model.entity.User;
import com.contentworkbench.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑，注册（BCrypt 加密 + 邮箱/用户名唯一校验）、登录验证
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String username, String email, String password) {
        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        userRepository.insert(user);
        return user;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return user;
    }
}
