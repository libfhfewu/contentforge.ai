package com.contentworkbench.service;

import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.model.entity.Workspace;
import com.contentworkbench.repository.AgentExecutionRepository;
import com.contentworkbench.repository.ContentVersionRepository;
import com.contentworkbench.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容版本管理，Agent 执行日志记录、内容版本存取
 */
@Service
public class ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentService.class);

    private final ContentVersionRepository cvRepo;
    private final AgentExecutionRepository aeRepo;
    private final WorkspaceRepository workspaceRepository;

    public ContentService(ContentVersionRepository cvRepo, AgentExecutionRepository aeRepo,
                          WorkspaceRepository workspaceRepository) {
        this.cvRepo = cvRepo;
        this.aeRepo = aeRepo;
        this.workspaceRepository = workspaceRepository;
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
        log.info("记录Agent执行日志: workspaceId={}, role={}", workspaceId, role);
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
        log.info("保存内容版本: workspaceId={}, title={}, platform={}", workspaceId, title, platform);
        return cv;
    }

    public List<ContentVersion> getVersions(Long workspaceId) {
        return cvRepo.findByWorkspaceId(workspaceId);
    }

    /**
     * 更新内容版本（带所有权校验）
     */
    public ContentVersion updateVersion(Long versionId, String title, String body, Long workspaceId) {
        ContentVersion cv = cvRepo.selectById(versionId);
        if (cv == null) throw new IllegalArgumentException("版本不存在");

        // 校验版本属于指定的工作区
        if (!cv.getWorkspaceId().equals(workspaceId)) {
            throw new IllegalArgumentException("版本不属于该工作区");
        }

        cv.setTitle(title);
        cv.setBody(body);
        cv.setIsUserEdited(1);
        cvRepo.updateById(cv);
        log.info("更新内容版本: versionId={}, workspaceId={}", versionId, workspaceId);
        return cv;
    }

    public List<AgentExecution> getAgentLogs(Long workspaceId) {
        return aeRepo.findByWorkspaceId(workspaceId);
    }
}
