package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.WorkspaceDTO;
import com.contentworkbench.model.entity.Workspace;
import com.contentworkbench.service.WorkspaceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工作区 CRUD 接口，创建、列表、详情查询
 */
@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    public ApiResponse<Workspace> create(@Valid @RequestBody WorkspaceDTO dto,
                                          @RequestAttribute("userId") Long userId) {
        Workspace ws = workspaceService.create(userId, dto.getTitle(), dto.getTopic());
        return ApiResponse.success(ws);
    }

    @GetMapping
    public ApiResponse<List<Workspace>> list(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(workspaceService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Workspace> detail(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        return ApiResponse.success(workspaceService.getById(id, userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestAttribute("userId") Long userId) {
        workspaceService.delete(id, userId);
        return ApiResponse.success(null);
    }
}
