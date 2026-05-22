package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.ContentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 内容版本数据访问层
 */
@Mapper
public interface ContentVersionRepository extends BaseMapper<ContentVersion> {
    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} AND platform = #{platform} ORDER BY version DESC")
    List<ContentVersion> findByWorkspaceAndPlatform(Long wsId, String platform);

    @Select("SELECT * FROM content_versions WHERE workspace_id = #{wsId} ORDER BY created_at DESC")
    List<ContentVersion> findByWorkspaceId(Long wsId);
}
