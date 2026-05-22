package com.contentworkbench.service;

import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.repository.AgentExecutionRepository;
import com.contentworkbench.repository.ContentVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer: persistence logic for agent execution logs and versioned content snapshots.
 */
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
