package com.contentworkbench.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component
public class DirectApiProvider implements LLMProvider {

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();

    public DirectApiProvider(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.api-url}") String apiUrl,
            @Value("${llm.model}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        HttpURLConnection conn = null;
        try {
            Map<String, Object> body = Map.of(
                "model", model,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "max_tokens", 4096
            );
            String json = mapper.writeValueAsString(body);

            conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(60_000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 400) {
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("LLM API error " + status + ": " + errorBody);
            }

            String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            var root = mapper.readTree(response);
            return root.path("content").get(0).path("text").asText();
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
        try {
            Map<String, Object> body = Map.of(
                "model", model,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "max_tokens", 4096,
                "stream", true
            );
            String json = mapper.writeValueAsString(body);

            HttpURLConnection conn = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(300_000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status >= 400) {
                String errorBody = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                conn.disconnect();
                throw new RuntimeException("LLM API error " + status + ": " + errorBody);
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            return reader.lines()
                .filter(line -> line.startsWith("data: ") && !line.contains("[DONE]"))
                .map(line -> {
                    try {
                        var root = mapper.readTree(line.substring(6));
                        String type = root.path("type").asText();
                        if ("content_block_delta".equals(type)) {
                            return root.path("delta").path("text").asText();
                        }
                        return "";
                    } catch (Exception e) { return ""; }
                })
                .filter(s -> !s.isEmpty())
                .onClose(() -> {
                    try { reader.close(); } catch (Exception ignored) {}
                    conn.disconnect();
                });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("LLM stream call failed", e);
        }
    }
}
