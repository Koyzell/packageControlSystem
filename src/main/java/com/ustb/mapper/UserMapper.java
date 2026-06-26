package com.ustb.mapper;

import com.ustb.entity.UserEntity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE id = #{id}")
    UserEntity findById(Long id);

    @Select("SELECT * FROM users WHERE username = #{username}")
    UserEntity findByUsername(String username);

    @Insert("INSERT INTO users (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserEntity entity);
}
