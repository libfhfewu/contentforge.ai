package com.contentworkbench.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * LLM API 直调实现，支持 Anthropic 和 OpenAI 兼容格式
 */
@Component
public class DirectApiProvider implements LLMProvider {

    private static final Logger log = LoggerFactory.getLogger(DirectApiProvider.class);

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final boolean isOpenAiCompatible;
    private final ObjectMapper mapper = new ObjectMapper();

    public DirectApiProvider(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.api-url}") String apiUrl,
            @Value("${llm.model}") String model,
            @Value("${llm.api-format:anthropic}") String apiFormat) {
        this.apiKey = apiKey;
        this.model = model;
        this.isOpenAiCompatible = "openai".equalsIgnoreCase(apiFormat);

        // 根据 API 格式设置正确的 URL
        if (this.isOpenAiCompatible) {
            // OpenAI 兼容格式: /v1/chat/completions
            this.apiUrl = apiUrl.contains("/chat/completions") ? apiUrl : apiUrl + "/chat/completions";
        } else {
            // Anthropic 格式: /v1/messages
            this.apiUrl = apiUrl.contains("/messages") ? apiUrl : apiUrl + "/messages";
        }
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        HttpURLConnection conn = null;
        try {
            String json;
            if (isOpenAiCompatible) {
                json = buildOpenAiRequest(systemPrompt, userMessage, false);
            } else {
                json = buildAnthropicRequest(systemPrompt, userMessage, false);
            }

            conn = createConnection();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 400) {
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("LLM API error " + status + ": " + errorBody);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return parseResponse(response);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("LLM API call failed", e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    public Stream<String> chatStream(String systemPrompt, String userMessage) {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            String json;
            if (isOpenAiCompatible) {
                json = buildOpenAiRequest(systemPrompt, userMessage, true);
            } else {
                json = buildAnthropicRequest(systemPrompt, userMessage, true);
            }

            conn = createConnection();
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setReadTimeout(300_000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 400) {
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("LLM API error " + status + ": " + errorBody);
            }

            reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            final HttpURLConnection finalConn = conn;
            final BufferedReader finalReader = reader;
            final boolean useOpenAi = isOpenAiCompatible;

            return reader.lines()
                .filter(line -> line.startsWith("data: ") && !line.contains("[DONE]"))
                .map(line -> {
                    try {
                        String data = line.substring(6);
                        if (useOpenAi) {
                            return parseOpenAiStreamChunk(data);
                        } else {
                            return parseAnthropicStreamChunk(data);
                        }
                    } catch (Exception e) { log.warn("流式数据解析失败: {}", e.getMessage()); return ""; }
                })
                .filter(s -> !s.isEmpty())
                .onClose(() -> {
                    try { finalReader.close(); } catch (Exception ignored) {}
                    finalConn.disconnect();
                });
        } catch (RuntimeException e) {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (conn != null) conn.disconnect();
            throw e;
        } catch (Exception e) {
            if (reader != null) {
                try { reader.close(); } catch (Exception ignored) {}
            }
            if (conn != null) conn.disconnect();
            throw new RuntimeException("LLM stream call failed", e);
        }
    }

    /**
     * 创建 HTTP 连接
     */
    private HttpURLConnection createConnection() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(60_000);
        conn.setDoOutput(true);

        if (isOpenAiCompatible) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        } else {
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
        }
        return conn;
    }

    /**
     * 构建 Anthropic 格式请求
     */
    private String buildAnthropicRequest(String systemPrompt, String userMessage, boolean stream) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", model);
        body.put("system", systemPrompt);
        body.put("messages", List.of(Map.of("role", "user", "content", userMessage)));
        body.put("max_tokens", 4096);
        if (stream) body.put("stream", true);
        return mapper.writeValueAsString(body);
    }

    /**
     * 构建 OpenAI 兼容格式请求
     */
    private String buildOpenAiRequest(String systemPrompt, String userMessage, boolean stream) throws Exception {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userMessage)
        ));
        body.put("max_tokens", 4096);
        if (stream) body.put("stream", true);
        return mapper.writeValueAsString(body);
    }

    /**
     * 解析响应（自动识别格式）
     */
    private String parseResponse(String response) throws Exception {
        JsonNode root = mapper.readTree(response);

        // 尝试 Anthropic 格式: {"content": [{"text": "..."}]}
        if (root.has("content") && root.get("content").isArray() && root.get("content").size() > 0) {
            return root.get("content").get(0).path("text").asText();
        }

        // 尝试 OpenAI 格式: {"choices": [{"message": {"content": "..."}}]}
        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            return root.get("choices").get(0).path("message").path("content").asText();
        }

        throw new RuntimeException("Unknown API response format: " + response.substring(0, Math.min(200, response.length())));
    }

    /**
     * 解析 Anthropic SSE 流式数据块
     */
    private String parseAnthropicStreamChunk(String data) throws Exception {
        var root = mapper.readTree(data);
        String type = root.path("type").asText();
        if ("content_block_delta".equals(type)) {
            return root.path("delta").path("text").asText();
        }
        return "";
    }

    /**
     * 解析 OpenAI SSE 流式数据块
     */
    private String parseOpenAiStreamChunk(String data) throws Exception {
        var root = mapper.readTree(data);
        var choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            var delta = choices.get(0).path("delta");
            if (delta.has("content")) {
                return delta.path("content").asText();
            }
        }
        return "";
    }
}
