package com.contentworkbench.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlatformAgentTest {

    @Mock private SpringAiLLMService springAiLLMService;
    @Mock private LLMProvider llmProvider;
    @InjectMocks private PlatformAgent platformAgent;

    @Test
    void shouldAdaptToAllThreePlatforms() {
        String wechatJson = "{\"title\":\"WX标题\",\"body\":\"WX正文\",\"coverSuggestion\":\"科技风\"}";
        String xhsJson = "{\"title\":\"XHS标题\",\"body\":\"XHS正文\",\"hashtags\":[\"#数码\"]}";
        String douyinJson = "{\"title\":\"DY标题\",\"hook\":\"前3秒\",\"script\":\"视频脚本\",\"hashtags\":[\"#热门\"],\"duration\":\"30秒\"}";

        when(springAiLLMService.chat(argThat(ps -> ps != null && ps.contains("WeChat Official Account")), anyString()))
            .thenReturn(wechatJson);
        when(springAiLLMService.chat(argThat(ps -> ps != null && ps.contains("Xiaohongshu")), anyString()))
            .thenReturn(xhsJson);
        when(springAiLLMService.chat(argThat(ps -> ps != null && ps.contains("抖音")), anyString()))
            .thenReturn(douyinJson);

        Map<String, String> results = platformAgent.execute("content text");

        assertThat(results).containsKeys("wechat", "xiaohongshu", "douyin");
        assertThat(results.get("wechat")).contains("WX标题");
        assertThat(results.get("xiaohongshu")).contains("#数码");
        assertThat(results.get("douyin")).contains("DY标题");
    }
}
