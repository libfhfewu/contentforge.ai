package com.contentworkbench.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.contentworkbench.model.entity.*;
import com.contentworkbench.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工作区服务 - 带缓存
 */
@Service
public class WorkspaceService {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceService.class);

    private final WorkspaceRepository workspaceRepository;
    private final AgentExecutionRepository agentExecutionRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final ContentPerformanceRepository contentPerformanceRepository;
    private final CompetitorAnalysisRepository competitorAnalysisRepository;
    private final MediaAssetRepository mediaAssetRepository;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            AgentExecutionRepository agentExecutionRepository,
                            ContentVersionRepository contentVersionRepository,
                            ContentPerformanceRepository contentPerformanceRepository,
                            CompetitorAnalysisRepository competitorAnalysisRepository,
                            MediaAssetRepository mediaAssetRepository) {
        this.workspaceRepository = workspaceRepository;
        this.agentExecutionRepository = agentExecutionRepository;
        this.contentVersionRepository = contentVersionRepository;
        this.contentPerformanceRepository = contentPerformanceRepository;
        this.competitorAnalysisRepository = competitorAnalysisRepository;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    /**
     * 创建工作区
     */
    public Workspace create(Long userId, String title, String topic) {
        Workspace ws = new Workspace();
        ws.setUserId(userId);
        ws.setTitle(title);
        ws.setTopic(topic);
        ws.setStatus(0);
        workspaceRepository.insert(ws);
        log.info("创建工作区: id={}, title={}", ws.getId(), title);
        return ws;
    }

    /**
     * 获取用户的工作区列表（带缓存）
     */
    @Cacheable(value = "workspaces", key = "#userId")
    public List<Workspace> listByUser(Long userId) {
        log.debug("从数据库查询工作区列表: userId={}", userId);
        return workspaceRepository.findByUserId(userId);
    }

    /**
     * 获取工作区详情（带缓存，缓存 key 包含 userId 防止授权绕过）
     */
    @Cacheable(value = "workspace", key = "#id + ':' + #userId")
    public Workspace getById(Long id, Long userId) {
        log.debug("从数据库查询工作区: id={}", id);
        Workspace ws = workspaceRepository.selectById(id);
        if (ws == null || !ws.getUserId().equals(userId)) {
            throw new IllegalArgumentException("工作区不存在或无权访问");
        }
        return ws;
    }

    /**
     * 更新工作区状态（清除缓存）
     */
    @CacheEvict(value = "workspace", key = "#id")
    public void updateStatus(Long id, Integer status) {
        Workspace ws = workspaceRepository.selectById(id);
        if (ws != null) {
            ws.setStatus(status);
            workspaceRepository.updateById(ws);
            log.info("更新工作区状态: id={}, status={}", id, status);
        }
    }

    /**
     * 内部更新状态（不清除缓存，用于 Agent 流程）
     */
    public void updateStatusInternal(Long id, Integer status) {
        Workspace ws = workspaceRepository.selectById(id);
        if (ws != null) {
            ws.setStatus(status);
            workspaceRepository.updateById(ws);
        }
    }

    /**
     * 更新工作区（清除缓存）
     */
    @CacheEvict(value = "workspace", key = "#id")
    public Workspace update(Long id, Long userId, String title, String topic) {
        Workspace ws = getById(id, userId);
        if (title != null) ws.setTitle(title);
        if (topic != null) ws.setTopic(topic);
        workspaceRepository.updateById(ws);
        log.info("更新工作区: id={}", id);
        return ws;
    }

    /**
     * 删除工作区（先删除所有关联数据，再删除工作区本身）
     */
    @Transactional
    @CacheEvict(value = {"workspace", "workspaces"}, allEntries = true)
    public void delete(Long id, Long userId) {
        Workspace ws = getById(id, userId);

        // 1. 删除内容效果数据
        contentPerformanceRepository.delete(new QueryWrapper<ContentPerformance>()
            .eq("workspace_id", id));

        // 2. 删除竞品分析数据
        competitorAnalysisRepository.delete(new QueryWrapper<CompetitorAnalysis>()
            .eq("workspace_id", id));

        // 3. 删除媒体资源数据
        mediaAssetRepository.delete(new QueryWrapper<MediaAsset>()
            .eq("workspace_id", id));

        // 4. 删除内容版本数据
        contentVersionRepository.delete(new QueryWrapper<ContentVersion>()
            .eq("workspace_id", id));

        // 5. 删除 Agent 执行记录
        agentExecutionRepository.delete(new QueryWrapper<AgentExecution>()
            .eq("workspace_id", id));

        // 6. 删除工作区
        workspaceRepository.deleteById(id);

        log.info("删除工作区及所有关联数据: id={}", id);
    }
}
