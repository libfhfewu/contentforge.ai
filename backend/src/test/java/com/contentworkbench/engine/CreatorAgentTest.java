package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatorAgentTest {

    @Mock private SpringAiLLMService springAiLLMService;
    @Mock private LLMProvider llmProvider;
    @InjectMocks private CreatorAgent creatorAgent;

    @Test
    void shouldReturnContentJson() {
        String json = "{\"title\":\"Title\",\"body\":\"## Intro\\nContent...\",\"tags\":[\"tag1\"],\"seoDesc\":\"desc\"}";
        when(springAiLLMService.chat(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(json);

        String result = creatorAgent.execute("{\"angles\":[\"A\"]}", "long");

        assertThat(result).contains("Title");
        assertThat(result).contains("## Intro");
    }
}
