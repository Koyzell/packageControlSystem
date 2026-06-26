package com.ustb.mapper;

import com.ustb.entity.CourierEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourierMapper {

    @Select("SELECT * FROM couriers ORDER BY id")
    List<CourierEntity> findAll();

    @Select("SELECT * FROM couriers WHERE name = #{name}")
    CourierEntity findByName(String name);

    @Insert("INSERT INTO couriers (name) VALUES (#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourierEntity entity);
}
