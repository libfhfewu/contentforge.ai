package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.ContentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * Repository layer: MyBatis mapper for the {@code content_versions} table — queries content snapshots by workspace and platform.
 */
@Mapper
public interface ContentVersionRepository extends BaseMapper<ContentVersion> {
    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} AND platform = #{platform} ORDER BY version DESC")
    List<ContentVersion> findByWorkspaceAndPlatform(Long wsId, String platform);

    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} ORDER BY created_at DESC")
    List<ContentVersion> findByWorkspaceId(Long wsId);
}
