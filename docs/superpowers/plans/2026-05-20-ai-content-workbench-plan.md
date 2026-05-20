# AI 内容创作工作台 — Phase 1 Java MVP 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建可演示的 AI 内容创作工作台 MVP：Vue 3 前端 + Spring Boot 后端 + 3 Agent 协作完成策略规划→内容创作→平台适配。

**Architecture:** Spring Boot 分层架构（Controller → Service → Engine → Repository），Engine 层用 LLMProvider 接口抽象，先实现 DirectApiProvider。前端 Vue 3 SPA，Pinia 状态管理，SSE 流式接收 Agent 输出。

**Tech Stack:** Java 17, Spring Boot 3, MyBatis-Plus, MySQL, Redis, Vue 3, TypeScript, Vite, Pinia, Naive UI, Claude API

**Not in this phase:** LangChain4j 实现、Python/Go 后端版本、DeepSeek API

---

## 文件结构

### 后端 (Spring Boot)

```
backend/
├── pom.xml
├── src/main/java/com/contentworkbench/
│   ├── ContentWorkbenchApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── LLMConfig.java
│   │   └── CorsConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── WorkspaceController.java
│   │   └── AgentController.java
│   ├── service/
│   │   ├── UserService.java
│   │   ├── WorkspaceService.java
│   │   ├── ContentService.java
│   │   └── AgentOrchestrator.java
│   ├── engine/
│   │   ├── LLMProvider.java
│   │   ├── DirectApiProvider.java
│   │   ├── StrategyAgent.java
│   │   ├── CreatorAgent.java
│   │   └── PlatformAgent.java
│   ├── model/
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── Workspace.java
│   │   │   ├── AgentExecution.java
│   │   │   └── ContentVersion.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── RegisterRequest.java
│   │       ├── AgentResponse.java
│   │       └── WorkspaceDTO.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── WorkspaceRepository.java
│   │   ├── AgentExecutionRepository.java
│   │   └── ContentVersionRepository.java
│   └── common/
│       ├── ApiResponse.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   └── db/schema.sql
└── src/test/java/com/contentworkbench/
    ├── service/
    │   ├── UserServiceTest.java
    │   ├── WorkspaceServiceTest.java
    │   └── AgentOrchestratorTest.java
    └── engine/
        ├── StrategyAgentTest.java
        ├── CreatorAgentTest.java
        └── PlatformAgentTest.java
```

### 前端 (Vue 3 + TS)

```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── router/index.ts
│   ├── stores/
│   │   ├── auth.ts
│   │   ├── workspace.ts
│   │   └── agent.ts
│   ├── api/
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   ├── workspace.ts
│   │   └── agent.ts
│   ├── views/
│   │   ├── LoginView.vue
│   │   ├── RegisterView.vue
│   │   ├── DashboardView.vue
│   │   ├── WorkspaceView.vue
│   │   └── HistoryView.vue
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AppHeader.vue
│   │   │   └── AppLayout.vue
│   │   ├── workspace/
│   │   │   ├── ProgressSteps.vue
│   │   │   ├── AgentChatPanel.vue
│   │   │   ├── StrategyCard.vue
│   │   │   ├── CreatorCard.vue
│   │   │   ├── PlatformCard.vue
│   │   │   ├── ContentEditor.vue
│   │   │   └── PlatformCompare.vue
│   │   └── common/
│   │       └── LoadingSpinner.vue
│   └── types/index.ts
```

---

## 任务列表

### Task 1: 项目脚手架 — Spring Boot

**Files:** Create `backend/pom.xml`, `backend/src/main/java/com/contentworkbench/ContentWorkbenchApplication.java`, `backend/src/main/resources/application.yml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.contentworkbench</groupId>
    <artifactId>content-workbench</artifactId>
    <version>0.1.0</version>
    <name>AI Content Workbench</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.6</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建启动类**

```java
package com.contentworkbench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ContentWorkbenchApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentWorkbenchApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/content_workbench?useUnicode=true&characterEncoding=utf-8
    username: root
    password: ${DB_PASSWORD:root}
  data:
    redis:
      host: localhost
      port: 6379

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto

llm:
  provider: direct-api
  api-key: ${LLM_API_KEY}
  api-url: https://api.anthropic.com/v1/messages
  model: claude-sonnet-4-6
```

- [ ] **Step 4: 验证项目能启动**

```bash
cd backend && mvn spring-boot:run
```

Expected: Spring Boot starts on port 8080.

---

### Task 2: 数据库 Schema

**Files:** Create `backend/src/main/resources/db/schema.sql`

- [ ] **Step 1: 创建 schema.sql**

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workspaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    topic TEXT,
    status TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS agent_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    agent_role VARCHAR(20) NOT NULL,
    input_prompt TEXT,
    output_content JSON,
    platform VARCHAR(20),
    tokens_used INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE IF NOT EXISTS content_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    agent_execution_id BIGINT,
    platform VARCHAR(20) NOT NULL DEFAULT '通用',
    title VARCHAR(500),
    body LONGTEXT,
    version INT DEFAULT 1,
    is_user_edited TINYINT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    FOREIGN KEY (agent_execution_id) REFERENCES agent_executions(id)
);
```

- [ ] **Step 2: 在 MySQL 中执行 schema**

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS content_workbench CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p content_workbench < backend/src/main/resources/db/schema.sql
```

---

### Task 3: 公共响应类 + 异常处理

**Files:** Create `backend/src/main/java/com/contentworkbench/common/ApiResponse.java`, `GlobalExceptionHandler.java`

- [ ] **Step 1: 创建 ApiResponse**

```java
package com.contentworkbench.common;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
```

- [ ] **Step 2: 创建 GlobalExceptionHandler**

```java
package com.contentworkbench.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequest(IllegalArgumentException e) {
        return ApiResponse.error(400, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleInternal(Exception e) {
        return ApiResponse.error(500, "Internal server error");
    }
}
```

---

### Task 4: User 实体 + Repository

**Files:** Create `backend/src/main/java/com/contentworkbench/model/entity/User.java`, `repository/UserRepository.java`

- [ ] **Step 1: 创建 User 实体**

```java
package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 2: 创建 UserRepository**

```java
package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
}
```

---

### Task 5: UserService — 注册登录

**Files:** Create `backend/src/main/java/com/contentworkbench/service/UserService.java`, `backend/src/test/java/com/contentworkbench/service/UserServiceTest.java`, Create `backend/src/main/java/com/contentworkbench/model/dto/LoginRequest.java`, `RegisterRequest.java`

- [ ] **Step 1: 创建 DTO**

```java
package com.contentworkbench.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 50)
    private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, max = 100)
    private String password;

    // getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

```java
package com.contentworkbench.model.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

- [ ] **Step 2: 写 UserService 测试**

```java
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
```

- [ ] **Step 3: 运行测试确认失败**

```bash
cd backend && mvn test -Dtest=UserServiceTest
```
Expected: FAIL — UserService not yet implemented.

- [ ] **Step 4: 实现 UserService**

```java
package com.contentworkbench.service;

import com.contentworkbench.model.entity.User;
import com.contentworkbench.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
```

- [ ] **Step 5: 运行测试确认通过**

```bash
cd backend && mvn test -Dtest=UserServiceTest
```
Expected: 4 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add backend/
git commit -m "feat: add User entity, repository, and UserService with register/login"
```

---

### Task 6: AuthController

**Files:** Create `backend/src/main/java/com/contentworkbench/controller/AuthController.java`

- [ ] **Step 1: 实现 AuthController**

```java
package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.LoginRequest;
import com.contentworkbench.model.dto.RegisterRequest;
import com.contentworkbench.model.entity.User;
import com.contentworkbench.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest req, HttpSession session) {
        User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword());
        session.setAttribute("userId", user.getId());
        return ApiResponse.success(user);
    }

    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        User user = userService.login(req.getEmail(), req.getPassword());
        session.setAttribute("userId", user.getId());
        return ApiResponse.success(user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.success(null);
    }
}
```

- [ ] **Step 2: 手动测试 API**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@test.com","password":"123456"}'
```
Expected: `{"code":200,"message":"ok","data":{...}}`

- [ ] **Step 3: Commit**

```bash
git add backend/
git commit -m "feat: add AuthController with register/login/logout"
```

---

### Task 7: Workspace 实体 + Repository

**Files:** Create `backend/src/main/java/com/contentworkbench/model/entity/Workspace.java`, `repository/WorkspaceRepository.java`

- [ ] **Step 1: 创建 Workspace 实体**

```java
package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("workspaces")
public class Workspace {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String topic;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: 创建 WorkspaceRepository**

```java
package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.Workspace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WorkspaceRepository extends BaseMapper<Workspace> {
    @Select("SELECT * FROM workspaces WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Workspace> findByUserId(Long userId);
}
```

---

### Task 8: WorkspaceService

**Files:** Create `backend/src/main/java/com/contentworkbench/service/WorkspaceService.java`, `backend/src/test/java/com/contentworkbench/service/WorkspaceServiceTest.java`, Create `backend/src/main/java/com/contentworkbench/model/dto/WorkspaceDTO.java`

- [ ] **Step 1: 创建 WorkspaceDTO**

```java
package com.contentworkbench.model.dto;

public class WorkspaceDTO {
    private Long id;
    private String title;
    private String topic;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
```

- [ ] **Step 2: 写 WorkspaceService 测试**

```java
package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentworkbench.model.entity.User;
import com.contentworkbench.model.entity.Workspace;
import com.contentworkbench.repository.UserRepository;
import com.contentworkbench.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class WorkspaceServiceTest {

    @Autowired private WorkspaceService workspaceService;
    @Autowired private WorkspaceRepository workspaceRepository;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        workspaceRepository.delete(null);
        userRepository.delete(null);
        User user = userService.register("wstest", "wstest@test.com", "pass123");
        userId = user.getId();
    }

    @Test
    void createShouldReturnWorkspace() {
        Workspace ws = workspaceService.create(userId, "测试工作区", "写一篇618数码促销文章");
        assertThat(ws.getId()).isNotNull();
        assertThat(ws.getTitle()).isEqualTo("测试工作区");
        assertThat(ws.getStatus()).isEqualTo(0);
    }

    @Test
    void listShouldReturnUserWorkspaces() {
        workspaceService.create(userId, "WS1", "topic1");
        workspaceService.create(userId, "WS2", "topic2");
        List<Workspace> list = workspaceService.listByUser(userId);
        assertThat(list).hasSize(2);
    }

    @Test
    void getDetailShouldReturnWorkspace() {
        Workspace created = workspaceService.create(userId, "Detail", "topic");
        Workspace found = workspaceService.getById(created.getId());
        assertThat(found.getTitle()).isEqualTo("Detail");
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

```bash
cd backend && mvn test -Dtest=WorkspaceServiceTest
```

- [ ] **Step 4: 实现 WorkspaceService**

```java
package com.contentworkbench.service;

import com.contentworkbench.model.entity.Workspace;
import com.contentworkbench.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    public Workspace create(Long userId, String title, String topic) {
        Workspace ws = new Workspace();
        ws.setUserId(userId);
        ws.setTitle(title);
        ws.setTopic(topic);
        ws.setStatus(0);
        workspaceRepository.insert(ws);
        return ws;
    }

    public List<Workspace> listByUser(Long userId) {
        return workspaceRepository.findByUserId(userId);
    }

    public Workspace getById(Long id) {
        Workspace ws = workspaceRepository.selectById(id);
        if (ws == null) throw new IllegalArgumentException("Workspace not found");
        return ws;
    }

    public void updateStatus(Long id, int status) {
        Workspace ws = getById(id);
        ws.setStatus(status);
        workspaceRepository.updateById(ws);
    }
}
```

- [ ] **Step 5: 运行测试确认通过**

```bash
cd backend && mvn test -Dtest=WorkspaceServiceTest
```

- [ ] **Step 6: Commit**

```bash
git add backend/
git commit -m "feat: add Workspace entity, repository, and service"
```

---

### Task 9: WorkspaceController + 登录拦截器

**Files:** Create `backend/src/main/java/com/contentworkbench/controller/WorkspaceController.java`, `config/SecurityConfig.java`

- [ ] **Step 1: 创建 Session 拦截器（内联在 SecurityConfig）**

```java
package com.contentworkbench.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }

    static class AuthInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":401,\"message\":\"Unauthorized\"}");
                return false;
            }
            request.setAttribute("userId", session.getAttribute("userId"));
            return true;
        }
    }
}
```

- [ ] **Step 2: 实现 WorkspaceController**

```java
package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.WorkspaceDTO;
import com.contentworkbench.model.entity.Workspace;
import com.contentworkbench.service.WorkspaceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ApiResponse<Workspace> create(@RequestBody WorkspaceDTO dto, @RequestAttribute("userId") Long userId) {
        Workspace ws = workspaceService.create(userId, dto.getTitle(), dto.getTopic());
        return ApiResponse.success(ws);
    }

    @GetMapping
    public ApiResponse<List<Workspace>> list(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(workspaceService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Workspace> detail(@PathVariable Long id) {
        return ApiResponse.success(workspaceService.getById(id));
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/
git commit -m "feat: add WorkspaceController with session-based auth interceptor"
```

---

### Task 10: LLMProvider 接口 + DirectApiProvider

**Files:** Create `backend/src/main/java/com/contentworkbench/engine/LLMProvider.java`, `DirectApiProvider.java`, `config/LLMConfig.java`

- [ ] **Step 1: 创建 LLMProvider 接口**

```java
package com.contentworkbench.engine;

import java.util.stream.Stream;

public interface LLMProvider {
    String chat(String systemPrompt, String userMessage);
    Stream<String> chatStream(String systemPrompt, String userMessage);
}
```

- [ ] **Step 2: 实现 DirectApiProvider**

```java
package com.contentworkbench.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Component
public class DirectApiProvider implements LLMProvider {

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public DirectApiProvider(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.api-url}") String apiUrl,
            @Value("${llm.model}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        try {
            String body = mapper.writeValueAsString(new ClaudeRequest(model, systemPrompt, userMessage, 4096));
            HttpURLConnection conn = createConnection(false);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode root = mapper.readTree(response);
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            throw new RuntimeException("LLM API call failed", e);
        }
    }

    @Override
    public Stream<String> chatStream(String systemPrompt, String userMessage) {
        try {
            String body = mapper.writeValueAsString(new ClaudeRequest(model, systemPrompt, userMessage, 4096));
            HttpURLConnection conn = createConnection(true);
            conn.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            return reader.lines()
                .filter(line -> line.startsWith("data: ") && !line.startsWith("data: [DONE]"))
                .map(line -> {
                    try {
                        JsonNode root = mapper.readTree(line.substring(6));
                        String type = root.path("type").asText();
                        if ("content_block_delta".equals(type)) {
                            return root.path("delta").path("text").asText();
                        }
                        return "";
                    } catch (Exception e) { return ""; }
                })
                .filter(s -> !s.isEmpty())
                .onClose(() -> { try { reader.close(); } catch (Exception e) {} });
        } catch (Exception e) {
            throw new RuntimeException("LLM stream call failed", e);
        }
    }

    private HttpURLConnection createConnection(boolean stream) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("x-api-key", apiKey);
        conn.setRequestProperty("anthropic-version", "2023-06-01");
        if (stream) conn.setRequestProperty("Accept", "text/event-stream");
        conn.setDoOutput(true);
        return conn;
    }

    record ClaudeRequest(String model, String system, String prompt, int maxTokens) {
        // serialized as: {"model":"..","system":"..","messages":[{"role":"user","content":".."}],"max_tokens":4096}
    }
}
```

- [ ] **Step 3: 创建 LLMConfig**

```java
package com.contentworkbench.config;

import com.contentworkbench.engine.LLMProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LLMConfig {

    @Bean
    @Primary
    public LLMProvider llmProvider(DirectApiProvider directApiProvider) {
        // Switch to LangChain4jProvider later by changing this bean
        return directApiProvider;
    }
}
```

---

### Task 11: StrategyAgent

**Files:** Create `backend/src/main/java/com/contentworkbench/engine/StrategyAgent.java`, `backend/src/test/java/com/contentworkbench/engine/StrategyAgentTest.java`

- [ ] **Step 1: 写 StrategyAgent 测试**

```java
package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyAgentTest {

    @Mock private LLMProvider llmProvider;
    @InjectMocks private StrategyAgent strategyAgent;

    @Test
    void shouldReturnStructuredPlan() {
        String mockResponse = """
        {
          "angles": ["性价比角度", "新品对比角度"],
          "keywords": ["618", "数码", "促销"],
          "structure": {"title": "618数码指南", "sections": ["前言","手机篇","电脑篇","总结"]},
          "publishPlan": ["预热:06-15", "爆发:06-18"]
        }
        """;
        when(llmProvider.chat(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(mockResponse);

        String result = strategyAgent.execute("618促销", "数码爱好者");

        assertThat(result).contains("性价比角度");
        assertThat(result).contains("618");
    }
}
```

- [ ] **Step 2: 实现 StrategyAgent**

```java
package com.contentworkbench.engine;

import org.springframework.stereotype.Component;

@Component
public class StrategyAgent {

    private final LLMProvider llmProvider;

    public StrategyAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String execute(String topic, String targetAudience) {
        String systemPrompt = """
        You are a senior content strategist. Given a topic and target audience, produce a content strategy plan.
        Output MUST be valid JSON with these keys:
        - angles: string[] - 2-4 content angles
        - keywords: string[] - 5-8 SEO keywords
        - structure: {title: string, sections: string[]}
        - publishPlan: string[] - key dates and what to publish
        Reply ONLY with valid JSON, no markdown, no explanation.
        """;

        String userMessage = String.format(
            "Topic: %s\nTarget audience: %s\nPlatforms: 公众号(长文), 小红书(种草), 推特(thread)",
            topic, targetAudience);

        return llmProvider.chat(systemPrompt, userMessage);
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend && mvn test -Dtest=StrategyAgentTest
```

- [ ] **Step 4: Commit**

```bash
git add backend/
git commit -m "feat: add StrategyAgent with structured content plan generation"
```

---

### Task 12: CreatorAgent + SSE 流式输出

**Files:** Create `backend/src/main/java/com/contentworkbench/engine/CreatorAgent.java`, `backend/src/test/java/com/contentworkbench/engine/CreatorAgentTest.java`

- [ ] **Step 1: 写 CreatorAgent 测试**

```java
package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatorAgentTest {

    @Mock private LLMProvider llmProvider;
    @InjectMocks private CreatorAgent creatorAgent;

    @Test
    void shouldReturnContentJson() {
        String json = "{\"title\":\"Title\",\"body\":\"## Intro\\nContent...\",\"tags\":[\"tag1\"],\"seoDesc\":\"desc\"}";
        when(llmProvider.chat(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(json);

        String result = creatorAgent.execute("{\"angles\":[\"A\"]}", "long");

        assertThat(result).contains("Title");
        assertThat(result).contains("## Intro");
    }

    @Test
    void shouldStreamContent() {
        when(llmProvider.chatStream(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(Stream.of("chunk1", "chunk2"));

        Stream<String> stream = creatorAgent.executeStream("plan", "short");
        var chunks = stream.toList();
        assertThat(chunks).containsExactly("chunk1", "chunk2");
    }
}
```

- [ ] **Step 2: 实现 CreatorAgent**

```java
package com.contentworkbench.engine;

import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class CreatorAgent {

    private final LLMProvider llmProvider;

    public CreatorAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String execute(String strategyJson, String contentType) {
        String systemPrompt = String.format("""
        You are a professional content writer. Create content based on the strategy plan.
        Content type: %s (long=深度长文, short=短文案)
        Output MUST be valid JSON:
        {
          "title": "article title",
          "body": "full markdown content",
          "tags": ["tag1", "tag2", ...],
          "seoDesc": "SEO meta description (for long content)"
        }
        Reply ONLY with valid JSON.
        """, contentType);

        return llmProvider.chat(systemPrompt, "Strategy plan:\n" + strategyJson);
    }

    public Stream<String> executeStream(String strategyJson, String contentType) {
        String systemPrompt = String.format("""
        You are a professional content writer. Create content based on the strategy plan.
        Content type: %s. Write in Chinese. Output full markdown.
        """, contentType);

        return llmProvider.chatStream(systemPrompt, "Strategy plan:\n" + strategyJson);
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend && mvn test -Dtest=CreatorAgentTest
```

- [ ] **Step 4: Commit**

```bash
git add backend/
git commit -m "feat: add CreatorAgent with streaming support"
```

---

### Task 13: PlatformAgent（支持并发）

**Files:** Create `backend/src/main/java/com/contentworkbench/engine/PlatformAgent.java`, `backend/src/test/java/com/contentworkbench/engine/PlatformAgentTest.java`

- [ ] **Step 1: 写 PlatformAgent 测试**

```java
package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformAgentTest {

    @Mock private LLMProvider llmProvider;
    @InjectMocks private PlatformAgent platformAgent;

    @Test
    void shouldAdaptToAllThreePlatforms() {
        String wechatJson = "{\"title\":\"WX标题\",\"body\":\"WX正文\",\"coverSuggestion\":\"科技风\"}";
        String xhsJson = "{\"title\":\"XHS标题\",\"body\":\"XHS正文\",\"hashtags\":[\"#数码\"]}";
        String twitterJson = "{\"thread\":[\"tweet1\",\"tweet2\"]}";

        when(llmProvider.chat(anyString(), anyString()))
            .thenReturn(wechatJson, xhsJson, twitterJson);

        Map<String, String> results = platformAgent.execute("content text");

        assertThat(results).containsKeys("wechat", "xiaohongshu", "twitter");
        assertThat(results.get("wechat")).contains("WX标题");
        assertThat(results.get("xiaohongshu")).contains("#数码");
        assertThat(results.get("twitter")).contains("tweet1");
    }
}
```

- [ ] **Step 2: 实现 PlatformAgent（并发）**

```java
package com.contentworkbench.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class PlatformAgent {

    private final LLMProvider llmProvider;

    public PlatformAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public Map<String, String> execute(String contentJson) {
        Map<String, String> results = new ConcurrentHashMap<>();

        CompletableFuture<Void> wechat = CompletableFuture.runAsync(() ->
            results.put("wechat", adapt("wechat", contentJson)));

        CompletableFuture<Void> xiaohongshu = CompletableFuture.runAsync(() ->
            results.put("xiaohongshu", adapt("xiaohongshu", contentJson)));

        CompletableFuture<Void> twitter = CompletableFuture.runAsync(() ->
            results.put("twitter", adapt("twitter", contentJson)));

        CompletableFuture.allOf(wechat, xiaohongshu, twitter).join();
        return results;
    }

    private String adapt(String platform, String contentJson) {
        String systemPrompt = switch (platform) {
            case "wechat" -> """
            You are a WeChat Official Account editor. Adapt content to:
            - Long-form, in-depth, professional tone
            - Add cover image suggestion
            Output JSON: {"title":"...", "body":"...", "coverSuggestion":"..."}
            """;
            case "xiaohongshu" -> """
            You are a Xiaohongshu (RED) content creator. Adapt content to:
            - Casual, enthusiastic tone with emojis
            - Short paragraphs, list format preferred
            - Include 3-5 hashtags
            Output JSON: {"title":"...", "body":"...", "hashtags":["#tag1",...]}
            """;
            case "twitter" -> """
            You are a Twitter/X content creator. Adapt content to:
            - Thread format, each tweet ≤ 280 chars
            - Hook first tweet, value in subsequent tweets
            Output JSON: {"thread":["tweet1","tweet2",...]}
            """;
            default -> throw new IllegalArgumentException("Unknown platform: " + platform);
        };

        return llmProvider.chat(systemPrompt, "Original content:\n" + contentJson);
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend && mvn test -Dtest=PlatformAgentTest
```

- [ ] **Step 4: Commit**

```bash
git add backend/
git commit -m "feat: add PlatformAgent with concurrent 3-platform adaptation"
```

---

### Task 14: AgentExecution 实体 + Repository + ContentService

**Files:** Create `backend/src/main/java/com/contentworkbench/model/entity/AgentExecution.java`, `ContentVersion.java`, `repository/AgentExecutionRepository.java`, `ContentVersionRepository.java`, `service/ContentService.java`

- [ ] **Step 1: 创建 AgentExecution 实体**

```java
package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("agent_executions")
public class AgentExecution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private String agentRole;
    private String inputPrompt;
    private String outputContent;
    private String platform;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public String getAgentRole() { return agentRole; }
    public void setAgentRole(String agentRole) { this.agentRole = agentRole; }
    public String getInputPrompt() { return inputPrompt; }
    public void setInputPrompt(String inputPrompt) { this.inputPrompt = inputPrompt; }
    public String getOutputContent() { return outputContent; }
    public void setOutputContent(String outputContent) { this.outputContent = outputContent; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 2: 创建 ContentVersion 实体**

```java
package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("content_versions")
public class ContentVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private Long agentExecutionId;
    private String platform;
    private String title;
    private String body;
    private Integer version;
    private Integer isUserEdited;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public Long getAgentExecutionId() { return agentExecutionId; }
    public void setAgentExecutionId(Long agentExecutionId) { this.agentExecutionId = agentExecutionId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getIsUserEdited() { return isUserEdited; }
    public void setIsUserEdited(Integer isUserEdited) { this.isUserEdited = isUserEdited; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 3: 创建 Repository 接口**

```java
package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.AgentExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AgentExecutionRepository extends BaseMapper<AgentExecution> {
    @Select("SELECT * FROM agent_executions WHERE workspace_id = #{workspaceId} ORDER BY created_at")
    List<AgentExecution> findByWorkspaceId(Long workspaceId);
}
```

```java
package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.ContentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ContentVersionRepository extends BaseMapper<ContentVersion> {
    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} AND platform = #{platform} ORDER BY version DESC")
    List<ContentVersion> findByWorkspaceAndPlatform(Long wsId, String platform);

    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} ORDER BY created_at DESC")
    List<ContentVersion> findByWorkspaceId(Long wsId);
}
```

- [ ] **Step 4: 实现 ContentService**

```java
package com.contentworkbench.service;

import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.repository.AgentExecutionRepository;
import com.contentworkbench.repository.ContentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {

    private final ContentVersionRepository cvRepo;
    private final AgentExecutionRepository aeRepo;

    public ContentService(ContentVersionRepository cvRepo, AgentExecutionRepository aeRepo) {
        this.cvRepo = cvRepo;
        this.aeRepo = aeRepo;
    }

    public AgentExecution logExecution(Long workspaceId, String role, String input, String output,
                                       String platform, int tokens) {
        AgentExecution ae = new AgentExecution();
        ae.setWorkspaceId(workspaceId);
        ae.setAgentRole(role);
        ae.setInputPrompt(input);
        ae.setOutputContent(output);
        ae.setPlatform(platform);
        ae.setTokensUsed(tokens);
        aeRepo.insert(ae);
        return ae;
    }

    public ContentVersion saveVersion(Long workspaceId, Long executionId, String platform,
                                      String title, String body, int versionNum) {
        ContentVersion cv = new ContentVersion();
        cv.setWorkspaceId(workspaceId);
        cv.setAgentExecutionId(executionId);
        cv.setPlatform(platform);
        cv.setTitle(title);
        cv.setBody(body);
        cv.setVersion(versionNum);
        cv.setIsUserEdited(0);
        cvRepo.insert(cv);
        return cv;
    }

    public List<ContentVersion> getVersions(Long workspaceId) {
        return cvRepo.findByWorkspaceId(workspaceId);
    }

    public ContentVersion updateVersion(Long versionId, String title, String body) {
        ContentVersion cv = cvRepo.selectById(versionId);
        if (cv == null) throw new IllegalArgumentException("Version not found");
        cv.setTitle(title);
        cv.setBody(body);
        cv.setIsUserEdited(1);
        cvRepo.updateById(cv);
        return cv;
    }

    public List<AgentExecution> getAgentLogs(Long workspaceId) {
        return aeRepo.findByWorkspaceId(workspaceId);
    }
}
```

---

### Task 15: AgentOrchestrator

**Files:** Create `backend/src/main/java/com/contentworkbench/service/AgentOrchestrator.java`, `backend/src/test/java/com/contentworkbench/service/AgentOrchestratorTest.java`

- [ ] **Step 1: 写 AgentOrchestrator 测试**

```java
package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.contentworkbench.engine.CreatorAgent;
import com.contentworkbench.engine.PlatformAgent;
import com.contentworkbench.engine.StrategyAgent;
import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentOrchestratorTest {

    @Mock private StrategyAgent strategyAgent;
    @Mock private CreatorAgent creatorAgent;
    @Mock private PlatformAgent platformAgent;
    @Mock private ContentService contentService;
    @Mock private WorkspaceService workspaceService;

    @InjectMocks private AgentOrchestrator orchestrator;

    @Test
    void shouldExecuteFullPipeline() {
        when(strategyAgent.execute(anyString(), anyString()))
            .thenReturn("{\"angles\":[\"A\"]}");
        when(contentService.logExecution(any(), anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(any(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());
        when(creatorAgent.execute(anyString(), anyString()))
            .thenReturn("{\"title\":\"T\",\"body\":\"B\"}");
        when(platformAgent.execute(anyString()))
            .thenReturn(Map.of("wechat", "wx", "xiaohongshu", "xhs", "twitter", "tw"));

        var result = orchestrator.runStrategy(1L, "topic", "audience");
        assertThat(result).contains("A");

        var contentResult = orchestrator.runCreator(1L, "strategy", "long");
        assertThat(contentResult).contains("T");

        var platformResult = orchestrator.runPlatformAdapters(1L, "content");
        assertThat(platformResult).containsKeys("wechat", "xiaohongshu", "twitter");
    }
}
```

- [ ] **Step 2: 实现 AgentOrchestrator**

```java
package com.contentworkbench.service;

import com.contentworkbench.engine.CreatorAgent;
import com.contentworkbench.engine.PlatformAgent;
import com.contentworkbench.engine.StrategyAgent;
import com.contentworkbench.model.entity.AgentExecution;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class AgentOrchestrator {

    private final StrategyAgent strategyAgent;
    private final CreatorAgent creatorAgent;
    private final PlatformAgent platformAgent;
    private final ContentService contentService;
    private final WorkspaceService workspaceService;

    public AgentOrchestrator(StrategyAgent strategyAgent, CreatorAgent creatorAgent,
                             PlatformAgent platformAgent, ContentService contentService,
                             WorkspaceService workspaceService) {
        this.strategyAgent = strategyAgent;
        this.creatorAgent = creatorAgent;
        this.platformAgent = platformAgent;
        this.contentService = contentService;
        this.workspaceService = workspaceService;
    }

    public String runStrategy(Long workspaceId, String topic, String audience) {
        workspaceService.updateStatus(workspaceId, 1); // 策划中
        String result = strategyAgent.execute(topic, audience);
        contentService.logExecution(workspaceId, "STRATEGY", "Topic: " + topic, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "策略计划", result, 1);
        workspaceService.updateStatus(workspaceId, 2); // 策划完成
        return result;
    }

    public String runCreator(Long workspaceId, String strategyJson, String contentType) {
        workspaceService.updateStatus(workspaceId, 2); // 创作中
        String result = creatorAgent.execute(strategyJson, contentType);
        contentService.logExecution(workspaceId, "CREATOR", strategyJson, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "内容草稿", result, 1);
        return result;
    }

    public Stream<String> runCreatorStream(Long workspaceId, String strategyJson, String contentType) {
        workspaceService.updateStatus(workspaceId, 2);
        return creatorAgent.executeStream(strategyJson, contentType);
    }

    public Map<String, String> runPlatformAdapters(Long workspaceId, String contentJson) {
        workspaceService.updateStatus(workspaceId, 3); // 适配中
        Map<String, String> results = platformAgent.execute(contentJson);
        results.forEach((platform, output) -> {
            contentService.logExecution(workspaceId, "PLATFORM", contentJson, output, platform, 0);
            contentService.saveVersion(workspaceId, null, platform, platform + "版", output, 1);
        });
        return results;
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
cd backend && mvn test -Dtest=AgentOrchestratorTest
```

---

### Task 16: AgentController

**Files:** Create `backend/src/main/java/com/contentworkbench/controller/AgentController.java`, `model/dto/AgentResponse.java`

- [ ] **Step 1: 创建 AgentResponse DTO**

```java
package com.contentworkbench.model.dto;

public class AgentResponse {
    private String agentRole;
    private String input;
    private Object output;
    private int tokensUsed;
    private long durationMs;

    public AgentResponse(String agentRole, String input, Object output, int tokensUsed, long durationMs) {
        this.agentRole = agentRole;
        this.input = input;
        this.output = output;
        this.tokensUsed = tokensUsed;
        this.durationMs = durationMs;
    }

    public String getAgentRole() { return agentRole; }
    public String getInput() { return input; }
    public Object getOutput() { return output; }
    public int getTokensUsed() { return tokensUsed; }
    public long getDurationMs() { return durationMs; }
}
```

- [ ] **Step 2: 实现 AgentController**

```java
package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.AgentResponse;
import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.service.AgentOrchestrator;
import com.contentworkbench.service.ContentService;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
public class AgentController {

    private final AgentOrchestrator orchestrator;
    private final ContentService contentService;

    public AgentController(AgentOrchestrator orchestrator, ContentService contentService) {
        this.orchestrator = orchestrator;
        this.contentService = contentService;
    }

    @PostMapping("/strategy/execute")
    public ApiResponse<AgentResponse> runStrategy(@PathVariable Long workspaceId,
                                                   @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        String result = orchestrator.runStrategy(workspaceId,
            body.get("topic"), body.getOrDefault("audience", "general"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("STRATEGY", body.get("topic"), result, 0, duration));
    }

    @PostMapping("/content/execute")
    public ApiResponse<AgentResponse> runContent(@PathVariable Long workspaceId,
                                                  @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        String result = orchestrator.runCreator(workspaceId,
            body.get("strategy"), body.getOrDefault("contentType", "long"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("CREATOR", body.get("strategy"), result, duration));
    }

    @GetMapping(value = "/content/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamContent(@PathVariable Long workspaceId,
                                     @RequestParam String strategy,
                                     @RequestParam(defaultValue = "long") String contentType) {
        SseEmitter emitter = new SseEmitter(300000L);
        var stream = orchestrator.runCreatorStream(workspaceId, strategy, contentType);
        stream.forEach(chunk -> {
            try {
                emitter.send(SseEmitter.event().data(Map.of("chunk", chunk, "agent", "CREATOR")));
            } catch (Exception e) { emitter.completeWithError(e); }
        });
        emitter.complete();
        return emitter;
    }

    @PostMapping("/platform/execute")
    public ApiResponse<AgentResponse> runPlatform(@PathVariable Long workspaceId,
                                                   @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        Map<String, String> results = orchestrator.runPlatformAdapters(workspaceId, body.get("content"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("PLATFORM", body.get("content"), results, 0, duration));
    }

    @GetMapping("/outputs")
    public ApiResponse<List<ContentVersion>> getOutputs(@PathVariable Long workspaceId) {
        return ApiResponse.success(contentService.getVersions(workspaceId));
    }

    @PutMapping("/outputs/{outputId}")
    public ApiResponse<ContentVersion> updateOutput(@PathVariable Long workspaceId,
                                                     @PathVariable Long outputId,
                                                     @RequestBody Map<String, String> body) {
        ContentVersion cv = contentService.updateVersion(outputId,
            body.get("title"), body.get("body"));
        return ApiResponse.success(cv);
    }

    @GetMapping("/agent-logs")
    public ApiResponse<List<AgentExecution>> getLogs(@PathVariable Long workspaceId) {
        return ApiResponse.success(contentService.getAgentLogs(workspaceId));
    }
}
```

---

### Task 17: 前端脚手架

**Files:** Create `frontend/` project with Vite + Vue 3 + TS

- [ ] **Step 1: 用 Vite 创建项目**

```bash
npm create vite@latest frontend -- --template vue-ts
cd frontend
npm install
npm install vue-router@4 pinia axios naive-ui @vicons/ionicons5
```

- [ ] **Step 2: 配置 vite.config.ts 代理**

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 3: 创建 router/index.ts**

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/dashboard' },
    { path: '/login', name: 'Login', component: () => import('../views/LoginView.vue') },
    { path: '/register', name: 'Register', component: () => import('../views/RegisterView.vue') },
    { path: '/dashboard', name: 'Dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
    { path: '/workspace/:id', name: 'Workspace', component: () => import('../views/WorkspaceView.vue'), meta: { requiresAuth: true } },
    { path: '/history', name: 'History', component: () => import('../views/HistoryView.vue'), meta: { requiresAuth: true } },
  ],
})

router.beforeEach((to, _from, next) => {
  if (to.meta.requiresAuth && !localStorage.getItem('userId')) {
    next('/login')
  } else {
    next()
  }
})

export default router
```

- [ ] **Step 4: 创建 api/client.ts**

```typescript
import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true,
})

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('userId')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default client
```

- [ ] **Step 5: 创建 main.ts 和 App.vue**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import naive from 'naive-ui'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(naive)
app.mount('#app')
```

```vue
<template>
  <n-message-provider>
    <router-view />
  </n-message-provider>
</template>
```

- [ ] **Step 6: Commit**

```bash
git add frontend/
git commit -m "feat: scaffold Vue 3 + TS frontend with router, pinia, naive-ui"
```

---

### Task 18: 认证页面（Login + Register）

**Files:** Create `frontend/src/views/LoginView.vue`, `RegisterView.vue`, `frontend/src/api/auth.ts`, `frontend/src/stores/auth.ts`

- [ ] **Step 1: 创建 auth API 层**

```typescript
// src/api/auth.ts
import client from './client'

export function login(email: string, password: string) {
  return client.post('/auth/login', { email, password })
}

export function register(username: string, email: string, password: string) {
  return client.post('/auth/register', { username, email, password })
}

export function logout() {
  return client.post('/auth/logout')
}
```

- [ ] **Step 2: 创建 auth store**

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as apiLogin, register as apiRegister, logout as apiLogout } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<{ id: number; username: string; email: string } | null>(null)

  async function doLogin(email: string, password: string) {
    const res = await apiLogin(email, password)
    user.value = res.data.data
    localStorage.setItem('userId', String(res.data.data.id))
  }

  async function doRegister(username: string, email: string, password: string) {
    const res = await apiRegister(username, email, password)
    user.value = res.data.data
    localStorage.setItem('userId', String(res.data.data.id))
  }

  async function doLogout() {
    await apiLogout()
    user.value = null
    localStorage.removeItem('userId')
  }

  return { user, doLogin, doRegister, doLogout }
})
```

- [ ] **Step 3: 创建 LoginView**

```vue
<!-- src/views/LoginView.vue -->
<template>
  <n-layout class="auth-layout">
    <n-card title="登录" class="auth-card">
      <n-form>
        <n-form-item label="邮箱">
          <n-input v-model:value="email" placeholder="your@email.com" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" placeholder="密码" />
        </n-form-item>
        <n-button type="primary" block @click="handleLogin" :loading="loading">登录</n-button>
      </n-form>
      <p class="auth-switch">没有账号？<router-link to="/register">注册</router-link></p>
    </n-card>
  </n-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function handleLogin() {
  loading.value = true
  try {
    await auth.doLogin(email.value, password.value)
    router.push('/dashboard')
  } catch (e: any) {
    alert(e.response?.data?.message || 'Login failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-layout { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f5f5; }
.auth-card { width: 400px; }
.auth-switch { text-align: center; margin-top: 16px; }
</style>
```

- [ ] **Step 4: 创建 RegisterView（同 LoginView 结构，增加 username 字段）**

```vue
<template>
  <n-layout class="auth-layout">
    <n-card title="注册" class="auth-card">
      <n-form>
        <n-form-item label="用户名">
          <n-input v-model:value="username" placeholder="用户名" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="email" placeholder="your@email.com" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" placeholder="至少6位" />
        </n-form-item>
        <n-button type="primary" block @click="handleRegister" :loading="loading">注册</n-button>
      </n-form>
      <p class="auth-switch">已有账号？<router-link to="/login">登录</router-link></p>
    </n-card>
  </n-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const email = ref('')
const password = ref('')
const loading = ref(false)
const router = useRouter()
const auth = useAuthStore()

async function handleRegister() {
  loading.value = true
  try {
    await auth.doRegister(username.value, email.value, password.value)
    router.push('/dashboard')
  } catch (e: any) {
    alert(e.response?.data?.message || 'Register failed')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-layout { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f5f5f5; }
.auth-card { width: 400px; }
.auth-switch { text-align: center; margin-top: 16px; }
</style>
```

- [ ] **Step 5: Commit**

```bash
git add frontend/
git commit -m "feat: add login and register pages with auth store"
```

---

### Task 19: Dashboard 页面

**Files:** Create `frontend/src/views/DashboardView.vue`, `frontend/src/api/workspace.ts`, `frontend/src/stores/workspace.ts`, `frontend/src/components/layout/AppHeader.vue`, `AppLayout.vue`

- [ ] **Step 1: 创建 workspace API**

```typescript
// src/api/workspace.ts
import client from './client'

export function createWorkspace(title: string, topic: string) {
  return client.post('/workspaces', { title, topic })
}

export function listWorkspaces() {
  return client.get('/workspaces')
}

export function getWorkspace(id: number) {
  return client.get(`/workspaces/${id}`)
}
```

- [ ] **Step 2: 创建 workspace store**

```typescript
// src/stores/workspace.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { createWorkspace, listWorkspaces } from '../api/workspace'

export interface Workspace {
  id: number
  title: string
  topic: string
  status: number
}

export const useWorkspaceStore = defineStore('workspace', () => {
  const workspaces = ref<Workspace[]>([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      const res = await listWorkspaces()
      workspaces.value = res.data.data
    } finally {
      loading.value = false
    }
  }

  async function create(title: string, topic: string) {
    const res = await createWorkspace(title, topic)
    workspaces.value.unshift(res.data.data)
    return res.data.data
  }

  return { workspaces, loading, fetchAll, create }
})
```

- [ ] **Step 3: 创建布局组件**

```vue
<!-- src/components/layout/AppHeader.vue -->
<template>
  <n-page-header>
    <template #title>
      <router-link to="/dashboard" style="text-decoration:none;color:inherit">AI 内容创作工作台</router-link>
    </template>
    <template #extra>
      <n-space>
        <n-button text @click="$router.push('/history')">历史记录</n-button>
        <n-button text @click="handleLogout">退出</n-button>
      </n-space>
    </template>
  </n-page-header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

async function handleLogout() {
  await auth.doLogout()
  router.push('/login')
}
</script>
```

```vue
<!-- src/components/layout/AppLayout.vue -->
<template>
  <n-layout class="app-layout">
    <AppHeader />
    <n-layout-content class="app-content">
      <slot />
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import AppHeader from './AppHeader.vue'
</script>

<style scoped>
.app-layout { min-height: 100vh; }
.app-content { max-width: 1200px; margin: 0 auto; padding: 24px; }
</style>
```

- [ ] **Step 4: 创建 DashboardView**

```vue
<template>
  <AppLayout>
    <n-space vertical size="large">
      <n-card title="新建工作区">
        <n-space vertical>
          <n-input v-model:value="newTitle" placeholder="工作区名称，如：618数码促销方案" />
          <n-input v-model:value="newTopic" type="textarea" placeholder="描述你的内容需求，如：写一篇面向数码爱好者的618促销长文..." :rows="3" />
          <n-button type="primary" @click="handleCreate" :loading="creating">开始创作</n-button>
        </n-space>
      </n-card>

      <n-card title="我的工作区">
        <n-grid v-if="store.workspaces.length > 0" cols="3" x-gap="12" y-gap="12">
          <n-grid-item v-for="ws in store.workspaces" :key="ws.id">
            <n-card :title="ws.title" hoverable @click="$router.push(`/workspace/${ws.id}`)">
              <n-ellipsis :line-clamp="2">{{ ws.topic }}</n-ellipsis>
              <n-tag :type="statusType(ws.status)" size="small">{{ statusText(ws.status) }}</n-tag>
            </n-card>
          </n-grid-item>
        </n-grid>
        <n-empty v-else description="还没有工作区，创建一个开始吧" />
      </n-card>
    </n-space>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useWorkspaceStore } from '../stores/workspace'
import AppLayout from '../components/layout/AppLayout.vue'

const router = useRouter()
const store = useWorkspaceStore()
const newTitle = ref('')
const newTopic = ref('')
const creating = ref(false)

onMounted(() => store.fetchAll())

async function handleCreate() {
  if (!newTitle.value || !newTopic.value) return
  creating.value = true
  try {
    const ws = await store.create(newTitle.value, newTopic.value)
    router.push(`/workspace/${ws.id}`)
  } finally {
    creating.value = false
  }
}

function statusText(s: number) {
  return ['草稿', '策划中', '创作中', '已完成'][s] || '草稿'
}

function statusType(s: number): 'default' | 'info' | 'warning' | 'success' {
  return ['default', 'info', 'warning', 'success'][s] as any
}
</script>
```

- [ ] **Step 5: Commit**

```bash
git add frontend/
git commit -m "feat: add dashboard with workspace creation and list"
```

---

### Task 20: WorkspaceView — Agent 交互页面

**Files:** Create `frontend/src/views/WorkspaceView.vue`, `frontend/src/api/agent.ts`, `frontend/src/stores/agent.ts`, Create workspace components

- [ ] **Step 1: 创建 agent API**

```typescript
// src/api/agent.ts
import client from './client'

export function executeStrategy(workspaceId: number, topic: string, audience: string) {
  return client.post(`/workspaces/${workspaceId}/strategy/execute`, { topic, audience })
}

export function executeContent(workspaceId: number, strategy: string, contentType: string) {
  return client.post(`/workspaces/${workspaceId}/content/execute`, { strategy, contentType })
}

export function executePlatform(workspaceId: number, content: string) {
  return client.post(`/workspaces/${workspaceId}/platform/execute`, { content })
}

export function getOutputs(workspaceId: number) {
  return client.get(`/workspaces/${workspaceId}/outputs`)
}

export function updateOutput(workspaceId: number, outputId: number, title: string, body: string) {
  return client.put(`/workspaces/${workspaceId}/outputs/${outputId}`, { title, body })
}

export function getAgentLogs(workspaceId: number) {
  return client.get(`/workspaces/${workspaceId}/agent-logs`)
}

export function streamContent(workspaceId: number, strategy: string, contentType: string) {
  return new EventSource(
    `/api/workspaces/${workspaceId}/content/stream?strategy=${encodeURIComponent(strategy)}&contentType=${contentType}`
  )
}
```

- [ ] **Step 2: 创建 agent store**

```typescript
// src/stores/agent.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/agent'

export const useAgentStore = defineStore('agent', () => {
  const strategyResult = ref<any>(null)
  const contentResult = ref<any>(null)
  const platformResults = ref<Record<string, any>>({})
  const currentStep = ref(0) // 0=topic, 1=strategy, 2=content, 3=platform
  const loading = ref(false)
  const streamChunks = ref<string[]>([])
  const outputs = ref<any[]>([])

  async function runStrategy(workspaceId: number, topic: string, audience: string) {
    loading.value = true
    const res = await api.executeStrategy(workspaceId, topic, audience)
    strategyResult.value = res.data.data.output
    currentStep.value = 1
    loading.value = false
  }

  async function runContent(workspaceId: number, strategy: string, contentType: string) {
    loading.value = true
    const res = await api.executeContent(workspaceId, strategy, contentType)
    contentResult.value = res.data.data.output
    currentStep.value = 2
    loading.value = false
  }

  async function runPlatform(workspaceId: number, content: string) {
    loading.value = true
    const res = await api.executePlatform(workspaceId, content)
    platformResults.value = res.data.data.output
    currentStep.value = 3
    loading.value = false
  }

  async function fetchOutputs(workspaceId: number) {
    const res = await api.getOutputs(workspaceId)
    outputs.value = res.data.data
  }

  return { strategyResult, contentResult, platformResults, currentStep, loading, streamChunks, outputs,
           runStrategy, runContent, runPlatform, fetchOutputs }
})
```

- [ ] **Step 3: 创建进度组件**

```vue
<!-- src/components/workspace/ProgressSteps.vue -->
<template>
  <n-steps :current="step" :status="step === 3 ? 'finish' : 'process'">
    <n-step title="策略规划" description="AI 分析选题策略" />
    <n-step title="内容创作" description="AI 撰写内容" />
    <n-step title="平台适配" description="适配三平台" />
  </n-steps>
</template>

<script setup lang="ts">
defineProps<{ step: number }>()
</script>
```

- [ ] **Step 4: 创建 StrategyCard 组件**

```vue
<!-- src/components/workspace/StrategyCard.vue -->
<template>
  <n-card title="🤖 策略规划师" size="small">
    <n-spin :show="loading">
      <div v-if="!result">
        <p>基于你的主题，AI 将分析并生成内容策略...</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">开始策划</n-button>
      </div>
      <div v-else>
        <pre class="result-json">{{ formatResult(result) }}</pre>
        <n-space>
          <n-button type="primary" @click="$emit('confirm')">确认策略，继续创作</n-button>
          <n-button @click="$emit('retry')">重试</n-button>
        </n-space>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
defineProps<{ result: any; loading: boolean }>()
defineEmits<{ execute: []; confirm: []; retry: [] }>()

function formatResult(r: any) {
  try { return JSON.stringify(typeof r === 'string' ? JSON.parse(r) : r, null, 2) }
  catch { return String(r) }
}
</script>

<style scoped>
.result-json { white-space: pre-wrap; background: #f8f8f8; padding: 12px; border-radius: 6px; max-height: 300px; overflow-y: auto; font-size: 13px; }
</style>
```

- [ ] **Step 5: 创建 CreatorCard 组件**

```vue
<!-- src/components/workspace/CreatorCard.vue -->
<template>
  <n-card title="✍️ 内容创作者" size="small">
    <n-spin :show="loading">
      <div v-if="!result">
        <p>策略已确认，开始生成内容...</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">生成内容</n-button>
      </div>
      <div v-else>
        <pre class="result-json">{{ formatResult(result) }}</pre>
        <ContentEditor :content="result" @update="(c: any) => $emit('edit', c)" />
        <n-space style="margin-top:8px">
          <n-button type="primary" @click="$emit('confirm')">确认内容，适配平台</n-button>
          <n-button @click="$emit('retry')">重试</n-button>
        </n-space>
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import ContentEditor from './ContentEditor.vue'
defineProps<{ result: any; loading: boolean }>()
defineEmits<{ execute: []; confirm: []; edit: [content: any]; retry: [] }>()

function formatResult(r: any) {
  try { return JSON.stringify(typeof r === 'string' ? JSON.parse(r) : r, null, 2) }
  catch { return String(r) }
}
</script>

<style scoped>
.result-json { white-space: pre-wrap; background: #f8f8f8; padding: 12px; border-radius: 6px; max-height: 300px; overflow-y: auto; font-size: 13px; }
</style>
```

- [ ] **Step 6: 创建 ContentEditor 组件**

```vue
<!-- src/components/workspace/ContentEditor.vue -->
<template>
  <n-card title="编辑内容" size="small">
    <n-form-item label="标题">
      <n-input v-model:value="editTitle" />
    </n-form-item>
    <n-form-item label="正文">
      <n-input v-model:value="editBody" type="textarea" :rows="8" />
    </n-form-item>
  </n-card>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ content: any }>()
const emit = defineEmits<{ update: [content: any] }>()

const editTitle = ref('')
const editBody = ref('')

watch(() => props.content, (c) => {
  if (c) {
    const parsed = typeof c === 'string' ? tryParse(c) : c
    editTitle.value = parsed?.title || ''
    editBody.value = parsed?.body || ''
  }
}, { immediate: true })

watch([editTitle, editBody], () => {
  emit('update', { title: editTitle.value, body: editBody.value })
})

function tryParse(s: string) { try { return JSON.parse(s) } catch { return {} } }
</script>
```

- [ ] **Step 7: 创建 PlatformCard + PlatformCompare**

```vue
<!-- src/components/workspace/PlatformCard.vue -->
<template>
  <n-card title="📱 平台优化师" size="small">
    <n-spin :show="loading">
      <div v-if="!hasResults">
        <p>内容已确认，将适配到公众号、小红书、推特三个平台...</p>
        <n-button type="primary" @click="$emit('execute')" :loading="loading">开始适配</n-button>
      </div>
      <div v-else>
        <PlatformCompare :results="results" @edit="(p, c) => $emit('edit', p, c)" />
      </div>
    </n-spin>
  </n-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PlatformCompare from './PlatformCompare.vue'

const props = defineProps<{ results: Record<string, any>; loading: boolean }>()
defineEmits<{ execute: []; edit: [platform: string, content: any] }>()

const hasResults = computed(() => props.results && Object.keys(props.results).length > 0)
</script>
```

```vue
<!-- src/components/workspace/PlatformCompare.vue -->
<template>
  <n-grid cols="3" x-gap="12">
    <n-grid-item v-for="platform in platforms" :key="platform.key">
      <n-card :title="platform.label" size="small">
        <n-input v-if="results[platform.key]" type="textarea" :value="formatPlatform(results[platform.key])" :rows="12" />
        <n-empty v-else description="等待生成..." />
      </n-card>
    </n-grid-item>
  </n-grid>
</template>

<script setup lang="ts">
const props = defineProps<{ results: Record<string, any> }>()
defineEmits<{ edit: [platform: string, content: any] }>()

const platforms = [
  { key: 'wechat', label: '📰 公众号' },
  { key: 'xiaohongshu', label: '📕 小红书' },
  { key: 'twitter', label: '🐦 推特' },
]

function formatPlatform(r: any) {
  try { return JSON.stringify(typeof r === 'string' ? JSON.parse(r) : r, null, 2) }
  catch { return String(r) }
}
</script>
```

- [ ] **Step 8: 创建主 WorkspaceView（组装所有组件）**

```vue
<template>
  <AppLayout>
    <n-space vertical size="large">
      <ProgressSteps :step="agent.currentStep" />

      <n-grid cols="2" x-gap="16">
        <n-grid-item>
          <n-space vertical>
            <n-card v-if="workspace" :title="workspace.title">
              <p>{{ workspace.topic }}</p>
            </n-card>
            <StrategyCard
              :result="agent.strategyResult"
              :loading="agent.loading"
              @execute="handleStrategy"
              @confirm="handleStrategyConfirm"
              @retry="handleStrategy"
            />
            <CreatorCard
              v-if="agent.currentStep >= 1"
              :result="agent.contentResult"
              :loading="agent.loading"
              @execute="handleContent"
              @confirm="handleContentConfirm"
              @retry="handleContent"
            />
            <PlatformCard
              v-if="agent.currentStep >= 2"
              :results="agent.platformResults"
              :loading="agent.loading"
              @execute="handlePlatform"
            />
          </n-space>
        </n-grid-item>
      </n-grid>
    </n-space>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getWorkspace } from '../api/workspace'
import { useAgentStore } from '../stores/agent'
import AppLayout from '../components/layout/AppLayout.vue'
import ProgressSteps from '../components/workspace/ProgressSteps.vue'
import StrategyCard from '../components/workspace/StrategyCard.vue'
import CreatorCard from '../components/workspace/CreatorCard.vue'
import PlatformCard from '../components/workspace/PlatformCard.vue'

const route = useRoute()
const agent = useAgentStore()
const workspace = ref<any>(null)
const workspaceId = Number(route.params.id)

onMounted(async () => {
  const res = await getWorkspace(workspaceId)
  workspace.value = res.data.data
})

async function handleStrategy() {
  await agent.runStrategy(workspaceId, workspace.value.topic, 'general')
}

function handleStrategyConfirm() {
  agent.currentStep = 1
}

async function handleContent() {
  const strategy = typeof agent.strategyResult === 'string'
    ? agent.strategyResult : JSON.stringify(agent.strategyResult)
  await agent.runContent(workspaceId, strategy, 'long')
}

function handleContentConfirm() {
  agent.currentStep = 2
}

async function handlePlatform() {
  const content = typeof agent.contentResult === 'string'
    ? agent.contentResult : JSON.stringify(agent.contentResult)
  await agent.runPlatform(workspaceId, content)
}
</script>
```

- [ ] **Step 9: Commit**

```bash
git add frontend/
git commit -m "feat: add WorkspaceView with 3-step Agent interaction flow"
```

---

### Task 21: HistoryView + CorsConfig

**Files:** Create `frontend/src/views/HistoryView.vue`, `backend/src/main/java/com/contentworkbench/config/CorsConfig.java`

- [ ] **Step 1: 创建 CorsConfig**

```java
package com.contentworkbench.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 2: 创建 HistoryView**

```vue
<template>
  <AppLayout>
    <n-card title="历史记录">
      <n-data-table :columns="columns" :data="store.workspaces" />
    </n-card>
  </AppLayout>
</template>

<script setup lang="ts">
import { h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton } from 'naive-ui'
import { useWorkspaceStore } from '../stores/workspace'
import AppLayout from '../components/layout/AppLayout.vue'

const store = useWorkspaceStore()
const router = useRouter()

const columns = [
  { title: '标题', key: 'title' },
  { title: '状态', key: 'status', render: (row: any) => ['草稿','策划中','创作中','已完成'][row.status] },
  { title: '时间', key: 'createdAt' },
  {
    title: '操作', key: 'actions',
    render: (row: any) => h(NButton, { size: 'small', onClick: () => router.push(`/workspace/${row.id}`) }, { default: () => '打开' })
  },
]

onMounted(() => store.fetchAll())
</script>
```

- [ ] **Step 3: Commit**

```bash
git add backend/ frontend/
git commit -m "feat: add history view and CORS config"
```

---

## 验证清单

完成所有任务后，执行以下验证：

- [ ] `mvn test` 全部通过
- [ ] `npm run dev` 前端启动，能注册/登录
- [ ] 创建工作区 → 触发策略Agent → 确认 → 触发创作Agent → 确认 → 触发平台Agent
- [ ] 三个平台输出正常展示
- [ ] 历史记录页面能访问之前的工作区

---

## 后续阶段（不在本计划中）

- **Phase 2**: LangChain4jProvider 实现，内容版本管理优化，UI 打磨
- **Phase 3**: Python FastAPI 后端，Go Gin 后端
- **Phase 4**: DeepSeek API，多模型对比，导出功能
