package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        User user = userService.register("wstest", "wstest" + System.currentTimeMillis() + "@test.com", "pass123");
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
        Workspace found = workspaceService.getById(created.getId(), userId);
        assertThat(found.getTitle()).isEqualTo("Detail");
    }

    @Test
    void getByIdNotFoundShouldThrow() {
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () ->
            workspaceService.getById(99999L, userId)
        ).getMessage()).contains("not found");
    }

    @Test
    void getByIdWithWrongUserShouldThrow() {
        Workspace created = workspaceService.create(userId, "Mine", "topic");
        // Create another user and try to access as them
        User otherUser = userService.register("other", "other" + System.currentTimeMillis() + "@test.com", "pass123");
        assertThrows(IllegalArgumentException.class, () ->
            workspaceService.getById(created.getId(), otherUser.getId()));
    }

    @Test
    void updateStatusWithWrongUserShouldThrow() {
        Workspace created = workspaceService.create(userId, "Mine", "topic");
        User otherUser = userService.register("other2", "other2" + System.currentTimeMillis() + "@test.com", "pass123");
        assertThrows(IllegalArgumentException.class, () ->
            workspaceService.updateStatus(created.getId(), 3, otherUser.getId()));
    }
}
