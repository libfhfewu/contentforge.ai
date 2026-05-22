package com.contentworkbench.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contentworkbench.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问层，MyBatis-Plus BaseMapper
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
}
