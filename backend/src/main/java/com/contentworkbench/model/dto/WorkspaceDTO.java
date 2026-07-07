package com.contentworkbench.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 工作区创建/更新请求体
 */
public class WorkspaceDTO {
    private Long id;

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在1-200之间")
    private String title;

    @NotBlank(message = "主题不能为空")
    @Size(min = 1, max = 5000, message = "主题长度不能超过5000")
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
