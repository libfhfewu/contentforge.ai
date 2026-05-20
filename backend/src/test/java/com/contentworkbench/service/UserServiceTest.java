package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.contentworkbench.model.entity.User;
import com.contentworkbench.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.delete(null);
    }

    @Test
    void registerShouldCreateUser() {
        User user = userService.register("testuser", "test@example.com", "password123");
        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPasswordHash()).isNotEqualTo("password123");
    }

    @Test
    void registerDuplicateEmailShouldThrow() {
        userService.register("user1", "dup@example.com", "pass123");
        assertThrows(IllegalArgumentException.class, () ->
            userService.register("user2", "dup@example.com", "pass456"));
    }

    @Test
    void loginShouldReturnUserWhenCredentialsMatch() {
        userService.register("loginuser", "login@example.com", "correct");
        User user = userService.login("login@example.com", "correct");
        assertThat(user).isNotNull();
    }

    @Test
    void loginShouldThrowWhenWrongPassword() {
        userService.register("baduser", "bad@example.com", "correct");
        assertThrows(IllegalArgumentException.class, () ->
            userService.login("bad@example.com", "wrong"));
    }
}
