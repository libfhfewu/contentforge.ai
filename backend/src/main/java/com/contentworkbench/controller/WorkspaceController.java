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
