package com.contentworkbench.engine;

import java.util.stream.Stream;

public interface LLMProvider {
    String chat(String systemPrompt, String userMessage);
    Stream<String> chatStream(String systemPrompt, String userMessage);
}
