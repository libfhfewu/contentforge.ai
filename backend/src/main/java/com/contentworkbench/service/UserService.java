package com.contentworkbench.service;

import com.contentworkbench.model.entity.User;
import com.contentworkbench.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑 - 带缓存
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 用户注册
     */
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
        user.setRole("USER");
        userRepository.insert(user);
        log.info("用户注册成功: username={}, email={}", username, email);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        log.info("用户登录成功: email={}", email);
        return user;
    }

    /**
     * 根据ID获取用户（带缓存）
     */
    @Cacheable(value = "user", key = "#id")
    public User getById(Long id) {
        log.debug("从数据库查询用户: id={}", id);
        return userRepository.selectById(id);
    }

    /**
     * 根据邮箱获取用户（带缓存）
     */
    @Cacheable(value = "userByEmail", key = "#email")
    public User getByEmail(String email) {
        log.debug("从数据库查询用户: email={}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * 更新用户信息（清除缓存）
     */
    @CacheEvict(value = {"user", "userByEmail"}, allEntries = true)
    public User update(User user) {
        userRepository.updateById(user);
        log.info("更新用户信息: id={}", user.getId());
        return user;
    }
}
