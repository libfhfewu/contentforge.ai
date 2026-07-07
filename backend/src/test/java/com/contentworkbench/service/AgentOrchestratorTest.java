package com.contentworkbench.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.contentworkbench.agent.reflect.ReflectionAgent;
import com.contentworkbench.agent.reflect.ReflectionResult;
import com.contentworkbench.engine.CreatorAgent;
import com.contentworkbench.engine.PlatformAgent;
import com.contentworkbench.engine.StrategyAgent;
import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import java.util.List;
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
    @Mock private com.contentworkbench.agent.memory.AgentMemory agentMemory;
    @Mock private com.contentworkbench.mq.MessageProducer messageProducer;
    @Mock private ReflectionAgent reflectionAgent;

    @InjectMocks private AgentOrchestrator orchestrator;

    @Test
    void shouldRunStrategy() {
        when(strategyAgent.execute(anyString(), anyString()))
            .thenReturn("{\"angles\":[\"A\"]}");
        when(reflectionAgent.reflect(anyString(), anyString(), anyString()))
            .thenReturn(new ReflectionResult(false, "good", List.of(), 0.9, null));
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
        when(reflectionAgent.reflect(anyString(), anyString(), anyString()))
            .thenReturn(new ReflectionResult(false, "good", List.of(), 0.9, null));
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), eq(null), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        String result = orchestrator.runCreator(1L, "strategy", "long");
        assertThat(result).contains("T");
    }

    @Test
    void shouldRetryCreatorOnLowScore() {
        when(creatorAgent.execute(anyString(), anyString()))
            .thenReturn("{\"title\":\"T\",\"body\":\"B\"}");
        // 第一次反思返回低分，第二次返回高分
        when(reflectionAgent.reflect(anyString(), anyString(), anyString()))
            .thenReturn(new ReflectionResult(true, "needs improvement", List.of("fix this"), 0.5, null))
            .thenReturn(new ReflectionResult(false, "good", List.of(), 0.85, null));
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), eq(null), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        String result = orchestrator.runCreator(1L, "strategy", "long");
        assertThat(result).contains("T");
        // creatorAgent 应该被调用两次（原 + 重试）
        verify(creatorAgent, times(2)).execute(anyString(), anyString());
    }

    @Test
    void shouldNotRetryCreatorOnHighScore() {
        when(creatorAgent.execute(anyString(), anyString()))
            .thenReturn("{\"title\":\"T\",\"body\":\"B\"}");
        when(reflectionAgent.reflect(anyString(), anyString(), anyString()))
            .thenReturn(new ReflectionResult(false, "excellent", List.of(), 0.95, null));
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), eq(null), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        String result = orchestrator.runCreator(1L, "strategy", "long");
        assertThat(result).contains("T");
        // creatorAgent 应该只被调用一次
        verify(creatorAgent, times(1)).execute(anyString(), anyString());
    }

    @Test
    void shouldRunPlatformAdapters() {
        when(platformAgent.execute(anyString()))
            .thenReturn(Map.of("wechat", "wx", "xiaohongshu", "xhs", "douyin", "dy"));
        when(contentService.logExecution(anyLong(), anyString(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new AgentExecution());
        when(contentService.saveVersion(anyLong(), any(), anyString(), anyString(), anyString(), anyInt()))
            .thenReturn(new ContentVersion());

        Map<String, String> result = orchestrator.runPlatformAdapters(1L, "content");
        assertThat(result).containsKeys("wechat", "xiaohongshu", "douyin");
    }
}
