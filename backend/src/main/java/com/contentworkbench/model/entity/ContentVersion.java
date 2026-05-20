package com.contentworkbench.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("content_versions")
public class ContentVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private Long agentExecutionId;
    private String platform;
    private String title;
    private String body;
    private Integer version;
    private Integer isUserEdited;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(Long workspaceId) { this.workspaceId = workspaceId; }
    public Long getAgentExecutionId() { return agentExecutionId; }
    public void setAgentExecutionId(Long agentExecutionId) { this.agentExecutionId = agentExecutionId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getIsUserEdited() { return isUserEdited; }
    public void setIsUserEdited(Integer isUserEdited) { this.isUserEdited = isUserEdited; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
