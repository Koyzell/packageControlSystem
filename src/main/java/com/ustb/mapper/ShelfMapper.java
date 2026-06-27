package com.ustb.mapper;

import com.ustb.entity.ShelfEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShelfMapper {

    @Select("SELECT * FROM shelves ORDER BY id")
    List<ShelfEntity> findAll();

    @Select("SELECT * FROM shelves WHERE name = #{name}")
    ShelfEntity findByName(String name);

    @Insert("INSERT INTO shelves (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShelfEntity entity);

    @Delete("DELETE FROM shelves WHERE name = #{name}")
    int deleteByName(String name);
}
