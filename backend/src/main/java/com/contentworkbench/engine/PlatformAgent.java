package com.contentworkbench.engine;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 平台适配 Agent：并发调用三平台（公众号/小红书/抖音），将内容转换为各平台风格
 */
@Component
public class PlatformAgent {

    private final SpringAiLLMService springAiLLMService;
    private final ExecutorService executorService;

    public PlatformAgent(SpringAiLLMService springAiLLMService,
                         @Qualifier("platformExecutor") ExecutorService executorService) {
        this.springAiLLMService = springAiLLMService;
        this.executorService = executorService;
    }

    public Map<String, String> execute(String contentJson) {
        Map<String, String> results = new ConcurrentHashMap<>();

        CompletableFuture<Void> wechat = CompletableFuture.runAsync(() -> {
            try {
                results.put("wechat", adapt("wechat", contentJson));
            } catch (Exception e) {
                results.put("wechat", "{\"error\":\"Adaptation failed: " + sanitizeErrorMessage(e) + "\"}");
            }
        }, executorService);

        CompletableFuture<Void> xiaohongshu = CompletableFuture.runAsync(() -> {
            try {
                results.put("xiaohongshu", adapt("xiaohongshu", contentJson));
            } catch (Exception e) {
                results.put("xiaohongshu", "{\"error\":\"Adaptation failed: " + sanitizeErrorMessage(e) + "\"}");
            }
        }, executorService);

        CompletableFuture<Void> douyin = CompletableFuture.runAsync(() -> {
            try {
                results.put("douyin", adapt("douyin", contentJson));
            } catch (Exception e) {
                results.put("douyin", "{\"error\":\"Adaptation failed: " + sanitizeErrorMessage(e) + "\"}");
            }
        }, executorService);

        CompletableFuture.allOf(wechat, xiaohongshu, douyin).orTimeout(60, TimeUnit.SECONDS).join();
        return results;
    }

    /**
     * 清理错误信息，避免泄露内部实现细节
     */
    private String sanitizeErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "Unknown error";
        }
        message = message.replaceAll("api[_-]?key[=:]\\s*\\S+", "api_key=***")
                        .replaceAll("password[=:]\\s*\\S+", "password=***")
                        .replaceAll("token[=:]\\s*\\S+", "token=***");
        if (message.length() > 200) {
            message = message.substring(0, 200) + "...";
        }
        return message;
    }

    private String adapt(String platform, String contentJson) {
        String systemPrompt;

        switch (platform) {
            case "wechat":
                systemPrompt = "You are a WeChat Official Account editor. Adapt content to:\n" +
                    "- Long-form, in-depth, professional tone\n" +
                    "- Add cover image suggestion\n" +
                    "Output JSON: {\"title\":\"...\", \"body\":\"...\", \"coverSuggestion\":\"...\"}";
                break;
            case "xiaohongshu":
                systemPrompt = "You are a Xiaohongshu (RED) content creator. Adapt content to:\n" +
                    "- Casual, enthusiastic tone with emojis\n" +
                    "- Short paragraphs, list format preferred\n" +
                    "- Include 3-5 hashtags\n" +
                    "Output JSON: {\"title\":\"...\", \"body\":\"...\", \"hashtags\":[\"#tag1\",...]}";
                break;
            case "douyin":
                systemPrompt = "你是一位抖音内容创作者。请将内容适配为抖音风格：\n" +
                    "- 短视频脚本格式，节奏紧凑\n" +
                    "- 前3秒必须有强吸引力的开头\n" +
                    "- 口语化、接地气的表达\n" +
                    "- 包含热门话题标签\n" +
                    "输出JSON: {\"title\":\"标题\",\"hook\":\"前3秒开头\",\"script\":\"视频脚本\",\"hashtags\":[\"#标签1\",...],\"duration\":\"建议时长\"}";
                break;
            default:
                throw new IllegalArgumentException("Unknown platform: " + platform);
        }

        return springAiLLMService.chat(systemPrompt, "Original content:\n" + contentJson);
    }
}
