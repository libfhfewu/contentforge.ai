package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * Agent 执行记录实体，映射 agent_executions 表
 */
@TableName("agent_executions")
public class AgentExecution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private String agentRole;
    private String inputPrompt;
    private String outputContent;
    private String platform;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public String getAgentRole() { return agentRole; }
    public void setAgentRole(String agentRole) { this.agentRole = agentRole; }
    public String getInputPrompt() { return inputPrompt; }
    public void setInputPrompt(String inputPrompt) { this.inputPrompt = inputPrompt; }
    public String getOutputContent() { return outputContent; }
    public void setOutputContent(String outputContent) { this.outputContent = outputContent; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
