package com.contentworkbench.controller;

import com.contentworkbench.common.ApiResponse;
import com.contentworkbench.model.dto.AgentResponse;
import com.contentworkbench.model.entity.AgentExecution;
import com.contentworkbench.model.entity.ContentVersion;
import com.contentworkbench.service.AgentOrchestrator;
import com.contentworkbench.service.ContentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * Controller layer: REST and SSE endpoints for running AI agents and managing content output within a workspace.
 */
@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
public class AgentController {

    private final AgentOrchestrator orchestrator;
    private final ContentService contentService;

    public AgentController(AgentOrchestrator orchestrator, ContentService contentService) {
        this.orchestrator = orchestrator;
        this.contentService = contentService;
    }

    @PostMapping("/strategy/execute")
    public ApiResponse<AgentResponse> runStrategy(@PathVariable Long workspaceId,
                                                   @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        String result = orchestrator.runStrategy(workspaceId,
            body.get("topic"), body.getOrDefault("audience", "general"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("STRATEGY", body.get("topic"), result, 0, duration));
    }

    @PostMapping("/content/execute")
    public ApiResponse<AgentResponse> runContent(@PathVariable Long workspaceId,
                                                  @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        String result = orchestrator.runCreator(workspaceId,
            body.get("strategy"), body.getOrDefault("contentType", "long"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("CREATOR", body.get("strategy"), result, 0, duration));
    }

    @GetMapping(value = "/content/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamContent(@PathVariable Long workspaceId,
                                     @RequestParam String strategy,
                                     @RequestParam(defaultValue = "long") String contentType) {
        SseEmitter emitter = new SseEmitter(300000L);
        var stream = orchestrator.runCreatorStream(workspaceId, strategy, contentType);
        stream.forEach(chunk -> {
            try {
                emitter.send(SseEmitter.event().data(Map.of("chunk", chunk, "agent", "CREATOR")));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        emitter.complete();
        return emitter;
    }

    @PostMapping("/platform/execute")
    public ApiResponse<AgentResponse> runPlatform(@PathVariable Long workspaceId,
                                                   @RequestBody Map<String, String> body) {
        long start = System.currentTimeMillis();
        Map<String, String> results = orchestrator.runPlatformAdapters(workspaceId, body.get("content"));
        long duration = System.currentTimeMillis() - start;
        return ApiResponse.success(new AgentResponse("PLATFORM", body.get("content"), results, 0, duration));
    }

    @GetMapping("/outputs")
    public ApiResponse<List<ContentVersion>> getOutputs(@PathVariable Long workspaceId) {
        return ApiResponse.success(contentService.getVersions(workspaceId));
    }

    @PutMapping("/outputs/{outputId}")
    public ApiResponse<ContentVersion> updateOutput(@PathVariable Long workspaceId,
                                                     @PathVariable Long outputId,
                                                     @RequestBody Map<String, String> body) {
        ContentVersion cv = contentService.updateVersion(outputId,
            body.get("title"), body.get("body"));
        return ApiResponse.success(cv);
    }

    @GetMapping("/agent-logs")
    public ApiResponse<List<AgentExecution>> getLogs(@PathVariable Long workspaceId) {
        return ApiResponse.success(contentService.getAgentLogs(workspaceId));
    }
}
