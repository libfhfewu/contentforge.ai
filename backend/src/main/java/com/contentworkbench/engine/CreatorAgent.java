package com.contentworkbench.engine;

import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class CreatorAgent {

    private final LLMProvider llmProvider;

    public CreatorAgent(LLMProvider llmProvider) {
        this.llmProvider = llmProvider;
    }

    public String execute(String strategyJson, String contentType) {
        String systemPrompt = String.format("""
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

        return llmProvider.chat(systemPrompt, "Strategy plan:\n" + strategyJson);
    }

    public Stream<String> executeStream(String strategyJson, String contentType) {
        String systemPrompt = String.format("""
        You are a professional content writer. Create content based on the strategy plan.
        Content type: %s. Write in Chinese. Output full markdown.
        """, contentType);

        return llmProvider.chatStream(systemPrompt, "Strategy plan:\n" + strategyJson);
    }
}
