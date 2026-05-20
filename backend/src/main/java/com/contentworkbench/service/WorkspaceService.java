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
