package com.contentworkbench.engine;

import java.util.stream.Stream;

/**
 * Engine layer: abstraction for LLM API calls, supporting both synchronous chat and streaming responses.
 */
public interface LLMProvider {
    String chat(String systemPrompt, String userMessage);
    Stream<String> chatStream(String systemPrompt, String userMessage);
}
