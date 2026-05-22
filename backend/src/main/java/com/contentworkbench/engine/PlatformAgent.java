package com.contentworkbench.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * Engine layer: AI agent that adapts content in parallel for multiple platforms (WeChat, Xiaohongshu, Twitter/X).
 */
@Component
public class PlatformAgent {

    private final LLMProvider llmProvider;

    public PlatformAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public Map<String, String> execute(String contentJson) {
        Map<String, String> results = new ConcurrentHashMap<>();

        CompletableFuture<Void> wechat = CompletableFuture.runAsync(() -> {
            try {
                results.put("wechat", adapt("wechat", contentJson));
            } catch (Exception e) {
                results.put("wechat", "{\"error\":\"Adaptation failed: " + e.getMessage() + "\"}");
            }
        });

        CompletableFuture<Void> xiaohongshu = CompletableFuture.runAsync(() -> {
            try {
                results.put("xiaohongshu", adapt("xiaohongshu", contentJson));
            } catch (Exception e) {
                results.put("xiaohongshu", "{\"error\":\"Adaptation failed: " + e.getMessage() + "\"}");
            }
        });

        CompletableFuture<Void> twitter = CompletableFuture.runAsync(() -> {
            try {
                results.put("twitter", adapt("twitter", contentJson));
            } catch (Exception e) {
                results.put("twitter", "{\"error\":\"Adaptation failed: " + e.getMessage() + "\"}");
            }
        });

        CompletableFuture.allOf(wechat, xiaohongshu, twitter).orTimeout(60, TimeUnit.SECONDS).join();
        return results;
    }

    private String adapt(String platform, String contentJson) {
        String systemPrompt = switch (platform) {
            case "wechat" -> """
            You are a WeChat Official Account editor. Adapt content to:
            - Long-form, in-depth, professional tone
            - Add cover image suggestion
            Output JSON: {"title":"...", "body":"...", "coverSuggestion":"..."}
            """;
            case "xiaohongshu" -> """
            You are a Xiaohongshu (RED) content creator. Adapt content to:
            - Casual, enthusiastic tone with emojis
            - Short paragraphs, list format preferred
            - Include 3-5 hashtags
            Output JSON: {"title":"...", "body":"...", "hashtags":["#tag1",...]}
            """;
            case "twitter" -> """
            You are a Twitter/X content creator. Adapt content to:
            - Thread format, each tweet ≤ 280 chars
            - Hook first tweet, value in subsequent tweets
            Output JSON: {"thread":["tweet1","tweet2",...]}
            """;
            default -> throw new IllegalArgumentException("Unknown platform: " + platform);
        };

        return llmProvider.chat(systemPrompt, "Original content:\n" + contentJson);
    }
}
