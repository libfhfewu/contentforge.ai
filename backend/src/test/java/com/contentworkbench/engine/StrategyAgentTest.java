package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyAgentTest {

    @Mock private LLMProvider llmProvider;
    @InjectMocks private StrategyAgent strategyAgent;

    @Test
    void shouldReturnStructuredPlan() {
        String mockResponse = """
        {
          "angles": ["性价比角度", "新品对比角度"],
          "keywords": ["618", "数码", "促销"],
          "structure": {"title": "618数码指南", "sections": ["前言","手机篇","电脑篇","总结"]},
          "publishPlan": ["预热:06-15", "爆发:06-18"]
        }
        """;
        when(llmProvider.chat(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
            .thenReturn(mockResponse);

        String result = strategyAgent.execute("618促销", "数码爱好者");

        assertThat(result).contains("性价比角度");
        assertThat(result).contains("618");
    }
}
