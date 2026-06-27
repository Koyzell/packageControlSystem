package com.ustb.mapper;

import com.ustb.entity.PackageEntity;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PackageMapper {

    @Insert("INSERT INTO packages (tracking_number, recipient_phone, courier_company, shelf_location, "
          + "pickup_code, status, check_in_time) "
          + "VALUES (#{trackingNumber}, #{recipientPhone}, #{courierCompany}, #{shelfLocation}, "
          + "#{pickupCode}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PackageEntity entity);

    @Select("SELECT * FROM packages WHERE id = #{id}")
    PackageEntity findById(Long id);

    @Select("SELECT * FROM packages WHERE tracking_number = #{trackingNumber}")
    PackageEntity findByTrackingNumber(String trackingNumber);

    @Select("SELECT * FROM packages WHERE status = 'AWAITING_PICKUP' "
          + "AND check_in_time < #{cutoffTime} ORDER BY check_in_time DESC")
    List<PackageEntity> findOverdue(LocalDateTime cutoffTime);

    @Update("UPDATE packages SET status = 'PICKED_UP', check_out_time = NOW() "
          + "WHERE id = #{id} AND status = 'AWAITING_PICKUP'")
    int updateStatusToPickedUp(Long id);

    @Select("SELECT COUNT(*) FROM packages WHERE check_in_time >= CURRENT_DATE()")
    int countToday();

    @Select("SELECT * FROM packages WHERE status = 'AWAITING_PICKUP' "
          + "AND (recipient_phone = #{keyword} OR pickup_code = #{keyword} OR tracking_number = #{keyword}) "
          + "ORDER BY check_in_time DESC")
    List<PackageEntity> findAwaitingByKeyword(String keyword);

    // XML-mapped: see resources/mapper/PackageMapper.xml
    List<PackageEntity> findAllWithPage(@Param("offset") int offset, @Param("pageSize") int pageSize,
                                        @Param("keyword") String keyword, @Param("status") String status);

    long countAll(@Param("keyword") String keyword, @Param("status") String status);

    // Dashboard
    @Select("SELECT COUNT(*) FROM packages WHERE DATE(check_in_time) = CURDATE()")
    long countTodayCheckIn();

    @Select("SELECT COUNT(*) FROM packages WHERE DATE(check_out_time) = CURDATE()")
    long countTodayPickup();

    @Select("SELECT COUNT(*) FROM packages WHERE status = 'AWAITING_PICKUP' "
          + "AND check_in_time < #{cutoffTime}")
    long countOverdue(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Select("SELECT COUNT(*) FROM packages WHERE status = 'AWAITING_PICKUP'")
    long countAwaiting();
}
