package com.contentworkbench.engine;

import java.util.stream.Stream;

/**
 * LLM 调用策略接口，定义 chat 和 chatStream 两个方法
 */
public interface LLMProvider {
    String chat(String systemPrompt, String userMessage);
    Stream<String> chatStream(String systemPrompt, String userMessage);
}
