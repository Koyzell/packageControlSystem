package com.ustb.mapper;

import com.ustb.entity.OperationLogEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OperationLogMapper {

    @Insert("INSERT INTO operation_logs (package_id, tracking_number, operation_type, operator, details) "
          + "VALUES (#{packageId}, #{trackingNumber}, #{operationType}, #{operator}, #{details})")
    int insert(OperationLogEntity entity);

    @Select("SELECT * FROM operation_logs ORDER BY created_at DESC LIMIT #{limit}")
    List<OperationLogEntity> findRecent(@Param("limit") int limit);
}
