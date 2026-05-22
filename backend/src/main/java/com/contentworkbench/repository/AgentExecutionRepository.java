package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.AgentExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * Repository layer: MyBatis mapper for the {@code agent_executions} table — queries execution logs by workspace.
 */
@Mapper
public interface AgentExecutionRepository extends BaseMapper<AgentExecution> {
    @Select("SELECT * FROM agent_executions WHERE workspace_id = #{workspaceId} ORDER BY created_at")
    List<AgentExecution> findByWorkspaceId(Long workspaceId);
}
