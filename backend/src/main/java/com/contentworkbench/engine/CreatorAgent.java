package com.contentworkbench.engine;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 内容创作 Agent：根据策略生成 Markdown 正文（支持同步和 SSE 流式两种模式）
 * 支持品牌风格注入
 */
@Component
public class CreatorAgent {

    private final SpringAiLLMService springAiLLMService;

    public CreatorAgent(SpringAiLLMService springAiLLMService) {
        this.springAiLLMService = springAiLLMService;
    }

    /**
     * 同步执行内容创作
     * @param strategyJson 策略 JSON
     * @param contentType 内容类型
     * @param stylePrompt 品牌风格 prompt（可选）
     */
    public String execute(String strategyJson, String contentType, String stylePrompt) {
        String basePrompt = String.format("""
        You are a professional content writer. Create content based on the strategy plan.
        Content type: %s (long=深度长文, short=短文案)
        Output MUST be valid JSON:
        {
          "title": "article title",
          "body": "full markdown content",
          "tags": ["tag1", "tag2", ...],
          "seoDesc": "SEO meta description (for long content)"
        }
        Reply ONLY with valid JSON.
        """, contentType);

        String systemPrompt = basePrompt;
        if (stylePrompt != null && !stylePrompt.isEmpty()) {
            systemPrompt = stylePrompt + "\n\n" + basePrompt;
        }

        return springAiLLMService.chat(systemPrompt, "Strategy plan:\n" + strategyJson);
    }

    /**
     * 流式执行 - 返回 Flux<String>
     * @param strategyJson 策略 JSON
     * @param contentType 内容类型
     * @param stylePrompt 品牌风格 prompt（可选）
     */
    public Flux<String> executeStream(String strategyJson, String contentType, String stylePrompt) {
        String basePrompt = String.format("""
        You are a professional content writer. Create content based on the strategy plan.
        Content type: %s. Write in Chinese. Output full markdown.
        """, contentType);

        String systemPrompt = basePrompt;
        if (stylePrompt != null && !stylePrompt.isEmpty()) {
            systemPrompt = stylePrompt + "\n\n" + basePrompt;
        }

        return springAiLLMService.chatStream(systemPrompt, "Strategy plan:\n" + strategyJson);
    }

    /**
     * 兼容旧调用
     */
    public String execute(String strategyJson, String contentType) {
        return execute(strategyJson, contentType, null);
    }

    /**
     * 兼容旧调用
     */
    public Flux<String> executeStream(String strategyJson, String contentType) {
        return executeStream(strategyJson, contentType, null);
    }
}
