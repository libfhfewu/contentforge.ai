package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.AgentResponse;
import com.contentworkbench.model.dto.ContentRequest;
import com.contentworkbench.model.dto.ParallelResearchRequest;
import com.contentworkbench.model.dto.PlatformRequest;
import com.contentworkbench.model.dto.StrategyRequest;
import com.contentworkbench.model.dto.UpdateOutputRequest;
import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.service.AgentOrchestrator;
import com.contentworkbench.service.ContentService;
import com.contentworkbench.service.WorkspaceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Agent 交互接口，策略/创作/平台适配的触发、SSE 流式输出、输出内容管理
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AgentOrchestrator orchestrator;
    private final ContentService contentService;
    private final WorkspaceService workspaceService;

    public AgentController(AgentOrchestrator orchestrator, ContentService contentService,
                           WorkspaceService workspaceService) {
        this.orchestrator = orchestrator;
        this.contentService = contentService;
        this.workspaceService = workspaceService;
    }

    /**
     * 从请求中获取当前用户 ID（由 AuthInterceptor 设置）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    /**
     * 校验工作区所有权
     */
    private void verifyOwnership(Long workspaceId, Long userId) {
        workspaceService.getById(workspaceId, userId);
    }

    @PostMapping("/strategy/execute")
    public ApiResponse<AgentResponse> runStrategy(@PathVariable Long workspaceId,
                                                   @Valid @RequestBody StrategyRequest request,
                                                   HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        Long brandProfileId = request.getBrandProfileId();
        long start = System.currentTimeMillis();
        String result = orchestrator.runStrategy(workspaceId, request.getTopic(), request.getAudience(), brandProfileId);
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("STRATEGY", request.getTopic(), result, 0, duration));
    }

    /**
     * 并行执行竞品分析和策略规划
     */
    @PostMapping("/strategy/parallel-research")
    public ApiResponse<?> runParallelResearchAndStrategy(
            @PathVariable Long workspaceId,
            @RequestBody ParallelResearchRequest request,
            HttpServletRequest httpRequest) {

        verifyOwnership(workspaceId, (Long) httpRequest.getAttribute("userId"));

        var result = orchestrator.runResearchAndStrategyParallel(
            workspaceId,
            request.getCompetitorName(),
            request.getTopic(),
            request.getAudience(),
            request.getPlatforms(),
            request.getBrandProfileId()
        );

        return ApiResponse.success(Map.of(
            "enrichedStrategy", result.enrichedStrategy(),
            "researchResult", result.researchResult(),
            "strategyResult", result.strategyResult()
        ));
    }

    @PostMapping("/content/execute")
    public ApiResponse<AgentResponse> runContent(@PathVariable Long workspaceId,
                                                  @Valid @RequestBody ContentRequest request,
                                                  HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        Long brandProfileId = request.getBrandProfileId();
        long start = System.currentTimeMillis();
        String result = orchestrator.runCreator(workspaceId, request.getStrategy(), request.getContentType(), brandProfileId);
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("CREATOR", request.getStrategy(), result, 0, duration));
    }

    /**
     * SSE 流式输出内容
     */
    @GetMapping(value = "/content/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamContent(@PathVariable Long workspaceId,
                                     @RequestParam String strategy,
                                     @RequestParam(defaultValue = "long") String contentType,
                                     @RequestParam(required = false) Long brandProfileId,
                                     HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        SseEmitter emitter = new SseEmitter(300000L);
        CompletableFuture.runAsync(() -> {
            StringBuilder output = new StringBuilder();
            try {
                orchestrator.runCreatorStream(workspaceId, strategy, contentType, brandProfileId)
                    .subscribe(
                        chunk -> {
                            try {
                                output.append(chunk);
                                emitter.send(SseEmitter.event().data(Map.of("chunk", chunk, "agent", "CREATOR")));
                            } catch (Exception e) {
                                throw new RuntimeException("SSE send failed", e);
                            }
                        },
                        error -> {
                            // 出错时也保存已生成的部分内容
                            String partialResult = output.toString();
                            if (!partialResult.isEmpty()) {
                                try {
                                    contentService.logExecution(workspaceId, "CREATOR", strategy, partialResult, null, 0);
                                    contentService.saveVersion(workspaceId, null, "通用", "内容草稿(部分)", partialResult, 1);
                                } catch (Exception saveEx) {
                                    log.warn("保存部分内容失败: workspaceId={}", workspaceId, saveEx);
                                }
                            }
                            try {
                                emitter.send(SseEmitter.event().name("error")
                                    .data(Map.of("agent", "CREATOR", "message", "内容生成失败", "partialSaved", !partialResult.isEmpty())));
                            } catch (Exception ignored) {
                            }
                            emitter.completeWithError(error);
                        },
                        () -> {
                            // 流式完成，保存已生成的内容
                            String result = output.toString();
                            if (!result.isEmpty()) {
                                contentService.logExecution(workspaceId, "CREATOR", strategy, result, null, 0);
                                contentService.saveVersion(workspaceId, null, "通用", "内容草稿", result, 1);
                            }
                            try {
                                emitter.send(SseEmitter.event().name("done").data(Map.of("agent", "CREATOR", "saved", true)));
                            } catch (Exception e) {
                                log.debug("发送SSE完成事件失败", e);
                            }
                            emitter.complete();
                        }
                    );
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @PostMapping("/platform/execute")
    public ApiResponse<AgentResponse> runPlatform(@PathVariable Long workspaceId,
                                                   @Valid @RequestBody PlatformRequest request,
                                                   HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        long start = System.currentTimeMillis();
        Map<String, String> results = orchestrator.runPlatformAdapters(workspaceId, request.getContent());
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("PLATFORM", request.getContent(), results, 0, duration));
    }

    @GetMapping("/outputs")
    public ApiResponse<List<ContentVersion>> getOutputs(@PathVariable Long workspaceId,
                                                         HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        return ApiResponse.success(contentService.getVersions(workspaceId));
    }

    @PutMapping("/outputs/{outputId}")
    public ApiResponse<ContentVersion> updateOutput(@PathVariable Long workspaceId,
                                                     @PathVariable Long outputId,
                                                     @Valid @RequestBody UpdateOutputRequest request,
                                                     HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        ContentVersion cv = contentService.updateVersion(outputId, request.getTitle(), request.getBody(), workspaceId);
        return ApiResponse.success(cv);
    }

    @GetMapping("/agent-logs")
    public ApiResponse<List<AgentExecution>> getLogs(@PathVariable Long workspaceId,
                                                      HttpServletRequest httpRequest) {
        verifyOwnership(workspaceId, getCurrentUserId(httpRequest));
        return ApiResponse.success(contentService.getAgentLogs(workspaceId));
    }
}
