package com.contentworkbench.engine;

import org.springframework.stereotype.Component;

/**
 * 策略规划 Agent：根据主题和目标受众生成内容策略
 * 支持品牌风格注入
 */
@Component
public class StrategyAgent {

    private final SpringAiLLMService springAiLLMService;

    public StrategyAgent(SpringAiLLMService springAiLLMService) {
        this.springAiLLMService = springAiLLMService;
    }

    /**
     * 执行策略生成
     * @param topic 主题
     * @param targetAudience 目标受众
     * @param stylePrompt 品牌风格 prompt（可选，为 null 或空则不注入）
     */
    public String execute(String topic, String targetAudience, String stylePrompt) {
        String basePrompt = """
        You are a senior content strategist. Given a topic and target audience, produce a content strategy plan.
        Output MUST be valid JSON with these keys:
        - angles: string[] - 2-4 content angles
        - keywords: string[] - 5-8 SEO keywords
        - structure: {title: string, sections: string[]}
        - publishPlan: string[] - key dates and what to publish
        Reply ONLY with valid JSON, no markdown, no explanation.
        """;

        // 注入品牌风格
        String systemPrompt = basePrompt;
        if (stylePrompt != null && !stylePrompt.isEmpty()) {
            systemPrompt = stylePrompt + "\n\n" + basePrompt;
        }

        String userMessage = String.format(
            "Topic: %s\nTarget audience: %s\nPlatforms: 公众号(长文), 小红书(种草), 抖音(短视频)",
            topic, targetAudience);

        return springAiLLMService.chat(systemPrompt, userMessage);
    }

    /**
     * 兼容旧调用
     */
    public String execute(String topic, String targetAudience) {
        return execute(topic, targetAudience, null);
    }
}
