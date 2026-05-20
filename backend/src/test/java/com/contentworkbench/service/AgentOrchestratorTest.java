package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldRunStrategy() {
        when(strategyAgent.execute(anyString(), anyString()))
            .thenReturn("{\"angles\":[\"A\"]}");
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), eq(null), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        String result = orchestrator.runStrategy(1L, "topic", "audience");
        assertThat(result).contains("A");
    }

    @Test
    void shouldRunCreator() {
        when(creatorAgent.execute(anyString(), anyString()))
            .thenReturn("{\"title\":\"T\",\"body\":\"B\"}");
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), eq(null), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        String result = orchestrator.runCreator(1L, "strategy", "long");
        assertThat(result).contains("T");
    }

    @Test
    void shouldRunPlatformAdapters() {
        when(platformAgent.execute(anyString()))
            .thenReturn(Map.of("wechat", "wx", "xiaohongshu", "xhs", "twitter", "tw"));
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        Map<String, String> result = orchestrator.runPlatformAdapters(1L, "content");
        assertThat(result).containsKeys("wechat", "xiaohongshu", "twitter");
    }
}
