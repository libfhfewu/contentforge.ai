package com.contentworkbench.model.dto;

/**
 * 工作区创建/更新请求体
 */
public class WorkspaceDTO {
    private Long id;
    private String title;
    private String topic;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
