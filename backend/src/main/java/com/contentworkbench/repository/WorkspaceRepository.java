package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.Workspace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * Repository layer: MyBatis mapper for the {@code workspaces} table — finds workspaces by user.
 */
@Mapper
public interface WorkspaceRepository extends BaseMapper<Workspace> {
    @Select("SELECT * FROM workspaces WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<Workspace> findByUserId(Long userId);
}
