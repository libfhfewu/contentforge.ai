package com.contentworkbench.service;

import com.contentworkbench.agent.memory.AgentMemory;
import com.contentworkbench.agent.reflect.ReflectionAgent;
import com.contentworkbench.agent.reflect.ReflectionResult;
import com.contentworkbench.engine.CreatorAgent;
import com.contentworkbench.engine.PlatformAgent;
import com.contentworkbench.engine.ResearchAgent;
import com.contentworkbench.engine.StrategyAgent;
import com.contentworkbench.mq.MessageProducer;
import com.contentworkbench.model.dto.AgentTaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Agent 编排器，支持同步和异步两种模式
 * 集成 ReflectionAgent 实现内容质量门控和自动重试
 * 支持品牌风格注入、竞品分析与策略规划并行执行
 */
@Service
public class AgentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestrator.class);
    private static final double QUALITY_THRESHOLD = 0.7;

    private final StrategyAgent strategyAgent;
    private final CreatorAgent creatorAgent;
    private final PlatformAgent platformAgent;
    private final ContentService contentService;
    private final WorkspaceService workspaceService;
    private final MessageProducer messageProducer;
    private final AgentMemory agentMemory;
    private final ReflectionAgent reflectionAgent;
    private final BrandStyleService brandStyleService;
    private final ResearchAgent researchAgent;

    public AgentOrchestrator(StrategyAgent strategyAgent, CreatorAgent creatorAgent,
                             PlatformAgent platformAgent, ContentService contentService,
                             WorkspaceService workspaceService, MessageProducer messageProducer,
                             AgentMemory agentMemory, ReflectionAgent reflectionAgent,
                             BrandStyleService brandStyleService, ResearchAgent researchAgent) {
        this.strategyAgent = strategyAgent;
        this.creatorAgent = creatorAgent;
        this.platformAgent = platformAgent;
        this.contentService = contentService;
        this.workspaceService = workspaceService;
        this.messageProducer = messageProducer;
        this.agentMemory = agentMemory;
        this.reflectionAgent = reflectionAgent;
        this.brandStyleService = brandStyleService;
        this.researchAgent = researchAgent;
    }

    /**
     * 并行执行竞品分析和策略规划，结果合并后传给创作 Agent
     *
     * @param workspaceId 工作区 ID
     * @param competitorName 竞品名称
     * @param topic 主题
     * @param audience 目标受众
     * @param platforms 分析平台
     * @param brandProfileId 品牌档案 ID
     * @return 并行执行结果（富化策略 + 竞品分析 + 原始策略）
     */
    public ParallelResult runResearchAndStrategyParallel(
            Long workspaceId, String competitorName, String topic, String audience,
            List<String> platforms, Long brandProfileId) {

        workspaceService.updateStatusInternal(workspaceId, 1);
        String sessionId = "workspace-" + workspaceId;
        String stylePrompt = getStylePrompt(brandProfileId);

        log.info("并行启动竞品分析和策略规划: workspace={}", workspaceId);

        // 并行执行竞品分析和策略规划
        CompletableFuture<String> researchFuture = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始竞品分析: competitor={}", competitorName);
                String result = researchAgent.analyze(competitorName, platforms,
                    List.of("titles", "keywords", "structure", "engagement"));
                log.info("竞品分析完成: {} 字符", result.length());
                return result;
            } catch (Exception e) {
                log.error("竞品分析失败", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        });

        CompletableFuture<String> strategyFuture = CompletableFuture.supplyAsync(() -> {
            try {
                log.info("开始策略规划: topic={}", topic);
                String result = strategyAgent.execute(topic, audience, stylePrompt);
                log.info("策略规划完成: {} 字符", result.length());
                return result;
            } catch (Exception e) {
                log.error("策略规划失败", e);
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }
        });

        // 等待全部完成（超时 90s）
        try {
            CompletableFuture.allOf(researchFuture, strategyFuture)
                .orTimeout(90, TimeUnit.SECONDS)
                .join();
        } catch (Exception e) {
            log.error("并行执行超时或失败", e);
        }

        String researchResult = researchFuture.join();
        String strategyResult = strategyFuture.join();

        // 合并结果：竞品洞察注入策略上下文
        String enrichedStrategy = strategyResult + "\n\n=== 竞品分析参考 ===\n" + researchResult;

        agentMemory.add(sessionId, "user", "请为以下主题生成策略: " + topic);
        agentMemory.add(sessionId, "assistant", "策略规划 + 竞品分析并行完成");

        contentService.logExecution(workspaceId, "RESEARCH_STRATEGY_PARALLEL",
            "Topic: " + topic + ", Competitor: " + competitorName,
            enrichedStrategy, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "策略计划(含竞品洞察)", enrichedStrategy, 1);

        log.info("并行执行完成: workspace={}", workspaceId);

        return new ParallelResult(enrichedStrategy, researchResult, strategyResult);
    }

    /**
     * 获取品牌风格 prompt
     */
    private String getStylePrompt(Long brandProfileId) {
        if (brandProfileId == null) return null;
        try {
            return brandStyleService.generateStylePrompt(brandProfileId);
        } catch (Exception e) {
            log.warn("获取品牌风格 prompt 失败: brandProfileId={}", brandProfileId, e);
            return null;
        }
    }

    /**
     * 同步执行策略（带质量反思和重试）
     */
    public String runStrategy(Long workspaceId, String topic, String audience) {
        return runStrategy(workspaceId, topic, audience, null);
    }

    /**
     * 同步执行策略（带品牌风格、质量反思和重试）
     */
    public String runStrategy(Long workspaceId, String topic, String audience, Long brandProfileId) {
        workspaceService.updateStatusInternal(workspaceId, 1);

        String sessionId = "workspace-" + workspaceId;
        agentMemory.add(sessionId, "user", "请为以下主题生成策略: " + topic + ", 目标受众: " + audience);

        String stylePrompt = getStylePrompt(brandProfileId);
        String result = strategyAgent.execute(topic, audience, stylePrompt);

        // 质量反思
        ReflectionResult reflection = reflectionAgent.reflect(sessionId, "策略规划: " + topic, result);
        if (reflection.qualityScore() < QUALITY_THRESHOLD) {
            log.info("策略质量偏低(score={})，重试一次", reflection.qualityScore());
            String feedback = "请改进以下策略。评语: " + reflection.critique();
            result = strategyAgent.execute(topic + "\n\n改进要求: " + feedback, audience, stylePrompt);
        }

        agentMemory.add(sessionId, "assistant", "策略生成完成:\n" + result);

        contentService.logExecution(workspaceId, "STRATEGY", "Topic: " + topic, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "策略计划", result, 1);
        return result;
    }

    /**
     * 同步执行内容创作（带质量反思和重试）
     */
    public String runCreator(Long workspaceId, String strategy, String contentType) {
        return runCreator(workspaceId, strategy, contentType, null);
    }

    /**
     * 同步执行内容创作（带品牌风格、质量反思和重试）
     */
    public String runCreator(Long workspaceId, String strategy, String contentType, Long brandProfileId) {
        workspaceService.updateStatusInternal(workspaceId, 2);

        String sessionId = "workspace-" + workspaceId;
        agentMemory.add(sessionId, "user", "请根据以下策略创作内容: " + strategy.substring(0, Math.min(100, strategy.length())) + "...");

        String stylePrompt = getStylePrompt(brandProfileId);
        String result = creatorAgent.execute(strategy, contentType, stylePrompt);

        // 质量反思 + 低分重试
        ReflectionResult reflection = reflectionAgent.reflect(sessionId, "内容创作: " + contentType, result);
        if (reflection.qualityScore() < QUALITY_THRESHOLD) {
            log.info("内容质量偏低(score={})，重试一次", reflection.qualityScore());
            String feedback = "请改进以下内容。评语: " + reflection.critique();
            result = creatorAgent.execute(strategy + "\n\n改进要求: " + feedback, contentType, stylePrompt);
        }

        agentMemory.add(sessionId, "assistant", "内容创作完成，共" + result.length() + "字");

        contentService.logExecution(workspaceId, "CREATOR", strategy, result, null, 0);
        contentService.saveVersion(workspaceId, null, "通用", "内容草稿", result, 1);
        return result;
    }

    /**
     * 流式执行内容创作
     */
    public Flux<String> runCreatorStream(Long workspaceId, String strategy, String contentType) {
        return runCreatorStream(workspaceId, strategy, contentType, null);
    }

    /**
     * 流式执行内容创作（带品牌风格）
     */
    public Flux<String> runCreatorStream(Long workspaceId, String strategy, String contentType, Long brandProfileId) {
        workspaceService.updateStatusInternal(workspaceId, 2);

        String sessionId = "workspace-" + workspaceId;
        agentMemory.add(sessionId, "user", "开始流式内容创作...");

        String stylePrompt = getStylePrompt(brandProfileId);
        return creatorAgent.executeStream(strategy, contentType, stylePrompt);
    }

    /**
     * 同步执行平台适配
     */
    public Map<String, String> runPlatformAdapters(Long workspaceId, String content) {
        workspaceService.updateStatusInternal(workspaceId, 3);

        String sessionId = "workspace-" + workspaceId;
        agentMemory.add(sessionId, "user", "开始平台适配...");

        Map<String, String> results = platformAgent.execute(content);

        StringBuilder summary = new StringBuilder("平台适配完成:\n");
        results.forEach((platform, output) -> {
            contentService.logExecution(workspaceId, "PLATFORM", content, output, platform, 0);
            contentService.saveVersion(workspaceId, null, platform, platform + "版本", output, 1);
            summary.append("- ").append(platform).append(": ").append(output.length()).append("字\n");
        });

        agentMemory.add(sessionId, "assistant", summary.toString());

        return results;
    }

    /**
     * 异步发送策略任务到消息队列
     */
    public void sendStrategyTaskAsync(Long workspaceId, String topic, String audience) {
        AgentTaskMessage message = new AgentTaskMessage();
        message.setWorkspaceId(workspaceId);
        message.setTaskType("STRATEGY");
        message.setTopic(topic);
        message.setAudience(audience);
        messageProducer.sendStrategyTask(message);
        log.info("策略任务已发送到消息队列: workspaceId={}", workspaceId);
    }

    /**
     * 异步发送内容创作任务到消息队列
     */
    public void sendContentTaskAsync(Long workspaceId, String strategy, String contentType) {
        AgentTaskMessage message = new AgentTaskMessage();
        message.setWorkspaceId(workspaceId);
        message.setTaskType("CONTENT");
        message.setStrategyJson(strategy);
        message.setContentType(contentType);
        messageProducer.sendContentTask(message);
        log.info("内容创作任务已发送到消息队列: workspaceId={}", workspaceId);
    }

    /**
     * 异步发送平台适配任务到消息队列
     */
    public void sendPlatformTaskAsync(Long workspaceId, String content) {
        AgentTaskMessage message = new AgentTaskMessage();
        message.setWorkspaceId(workspaceId);
        message.setTaskType("PLATFORM");
        message.setContentJson(content);
        messageProducer.sendPlatformTask(message);
        log.info("平台适配任务已发送到消息队列: workspaceId={}", workspaceId);
    }

    /**
     * 并行执行结果记录
     *
     * @param enrichedStrategy 合并竞品分析后的富化策略
     * @param researchResult 竞品分析结果
     * @param strategyResult 原始策略结果
     */
    public record ParallelResult(String enrichedStrategy, String researchResult, String strategyResult) {}
}
