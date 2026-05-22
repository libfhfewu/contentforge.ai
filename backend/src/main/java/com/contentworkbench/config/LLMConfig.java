package com.contentworkbench.config;

import com.contentworkbench.engine.DirectApiProvider;
import com.contentworkbench.engine.LLMProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LLM 引擎配置：暴露 LLMProvider Bean，后续切换 LangChain4j 只需改此处
 */
@Configuration
public class LLMConfig {

    @Bean
    @Primary
    public LLMProvider llmProvider(DirectApiProvider directApiProvider) {
        // Later: switch to LangChain4jProvider by changing this bean
        return directApiProvider;
    }
}
