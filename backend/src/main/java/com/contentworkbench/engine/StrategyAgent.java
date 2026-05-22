package com.contentworkbench.engine;

import org.springframework.stereotype.Component;

/**
 * Engine layer: AI agent that generates a structured content strategy plan (angles, keywords, publish plan) from a topic.
 */
@Component
public class StrategyAgent {

    private final LLMProvider llmProvider;

    public StrategyAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String execute(String topic, String targetAudience) {
        String systemPrompt = """
        You are a senior content strategist. Given a topic and target audience, produce a content strategy plan.
        Output MUST be valid JSON with these keys:
        - angles: string[] - 2-4 content angles
        - keywords: string[] - 5-8 SEO keywords
        - structure: {title: string, sections: string[]}
        - publishPlan: string[] - key dates and what to publish
        Reply ONLY with valid JSON, no markdown, no explanation.
        """;

        String userMessage = String.format(
            "Topic: %s\nTarget audience: %s\nPlatforms: 公众号(长文), 小红书(种草), 推特(thread)",
            topic, targetAudience);

        return llmProvider.chat(systemPrompt, userMessage);
    }
}
