package com.contentworkbench.service;

import com.contentworkbench.engine.CreatorAgent;
import com.contentworkbench.engine.PlatformAgent;
import com.contentworkbench.engine.StrategyAgent;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Agent 编排器，顺序调度 3 个 Agent（策略→创作→平台），管理状态流转
 */
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
        workspaceService.updateStatusInternal(workspaceId, 1);
        String result = strategyAgent.execute(topic, audience);
        contentService.logExecution(workspaceId, "STRATEGY", "Topic: " + topic, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "策略计划", result, 1);
        return result;
    }

    public String runCreator(Long workspaceId, String strategyJson, String contentType) {
        workspaceService.updateStatusInternal(workspaceId, 2);
        String result = creatorAgent.execute(strategyJson, contentType);
        contentService.logExecution(workspaceId, "CREATOR", strategyJson, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "内容草稿", result, 1);
        return result;
    }

    public Stream<String> runCreatorStream(Long workspaceId, String strategyJson, String contentType) {
        workspaceService.updateStatusInternal(workspaceId, 2);
        return creatorAgent.executeStream(strategyJson, contentType);
    }

    public Map<String, String> runPlatformAdapters(Long workspaceId, String contentJson) {
        Map<String, String> results = platformAgent.execute(contentJson);
        results.forEach((platform, output) -> {
            contentService.logExecution(workspaceId, "PLATFORM", contentJson, output, platform, 0);
            contentService.saveVersion(workspaceId, null, platform, platform + "版", output, 1);
        });
        return results;
    }
}
