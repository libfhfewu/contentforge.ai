package com.contentworkbench.model.dto;

/**
 * Agent 执行响应体（角色、输入、输出、token 消耗、耗时）
 */
public class AgentResponse {
    private String agentRole;
    private String input;
    private Object output;
    private int tokensUsed;
    private long durationMs;

    public AgentResponse(String agentRole, String input, Object output, int tokensUsed, long durationMs) {
        this.agentRole = agentRole;
        this.input = input;
        this.output = output;
        this.tokensUsed = tokensUsed;
        this.durationMs = durationMs;
    }

    public String getAgentRole() { return agentRole; }
    public String getInput() { return input; }
    public Object getOutput() { return output; }
    public int getTokensUsed() { return tokensUsed; }
    public long getDurationMs() { return durationMs; }
}
