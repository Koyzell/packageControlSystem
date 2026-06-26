package com.ustb.service.impl;

import com.ustb.dto.*;
import com.ustb.entity.OperationLogEntity;
import com.ustb.entity.PackageEntity;
import com.ustb.enums.PackageStatus;
import com.ustb.exception.BusinessException;
import com.ustb.mapper.OperationLogMapper;
import com.ustb.mapper.PackageMapper;
import com.ustb.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {

    private static final int MAX_PER_LETTER = 999;
    private static final int MAX_LETTERS = 26;

    private final PackageMapper packageMapper;
    private final OperationLogMapper operationLogMapper;

    @Override
    public PackageVO checkIn(CheckInRequest request, String operator) {
        PackageEntity existing = packageMapper.findByTrackingNumber(request.getTrackingNumber());
        if (existing != null) {
            throw new BusinessException(409, "该运单号已入库");
        }

        PackageEntity entity = request.toEntity();
        entity.setPickupCode(generatePickupCode());
        packageMapper.insert(entity);
        entity = packageMapper.findById(entity.getId());

        operationLogMapper.insert(OperationLogEntity.builder()
                .packageId(entity.getId())
                .trackingNumber(entity.getTrackingNumber())
                .operationType("CHECK_IN")
                .operator(operator)
                .details("入库: " + entity.getCourierCompany() + " | 货架: " + entity.getShelfLocation())
                .build());

        return toVO(entity);
    }

    @Override
    public PackageVO pickup(Long id, String operator) {
        PackageEntity entity = packageMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(404, "包裹不存在");
        }
        if (!PackageStatus.AWAITING_PICKUP.name().equals(entity.getStatus())) {
            throw new BusinessException(400, "该包裹已被取走");
        }
        packageMapper.updateStatusToPickedUp(id);
        entity.setStatus(PackageStatus.PICKED_UP.name());
        entity.setCheckOutTime(LocalDateTime.now());

        operationLogMapper.insert(OperationLogEntity.builder()
                .packageId(entity.getId())
                .trackingNumber(entity.getTrackingNumber())
                .operationType("PICK_UP")
                .operator(operator)
                .details("取件: " + entity.getPickupCode())
                .build());

        log.info("取件成功 - 操作人: {}, 运单号: {}, 取件码: {}, 取件时间: {}",
                operator, entity.getTrackingNumber(), entity.getPickupCode(),
                entity.getCheckOutTime());

        return toVO(entity);
    }

    @Override
    public List<PackageVO> findAll(String status) {
        List<PackageEntity> entities;
        if (status != null && !status.isEmpty()) {
            entities = packageMapper.findByStatus(status);
        } else {
            entities = packageMapper.findAll();
        }
        return entities.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<PackageVO> findOverdue() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
        return packageMapper.findOverdue(cutoffTime).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PackageVO> findAwaitingByPhone(String phone) {
        return packageMapper.findAwaitingByPhone(phone).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PackageVO> findAwaitingByKeyword(String keyword) {
        return packageMapper.findAwaitingByKeyword(keyword).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<PackageVO> findAllWithPage(int page, int pageSize, String keyword, String status) {
        int offset = (page - 1) * pageSize;
        List<PackageEntity> entities = packageMapper.findAllWithPage(offset, pageSize, keyword, status);
        long total = packageMapper.countAll(keyword, status);
        List<PackageVO> vos = entities.stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, total, page, pageSize);
    }

    @Override
    public DashboardVO getDashboard() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(48);
        return DashboardVO.builder()
                .todayCheckIn(packageMapper.countTodayCheckIn())
                .todayPickup(packageMapper.countTodayPickup())
                .overdueCount(packageMapper.countOverdue(cutoffTime))
                .awaitingCount(packageMapper.countAwaiting())
                .build();
    }

    @Override
    public List<OperationLogVO> getRecentLogs(int limit) {
        return operationLogMapper.findRecent(limit).stream()
                .map(log -> OperationLogVO.builder()
                        .id(log.getId())
                        .packageId(log.getPackageId())
                        .trackingNumber(log.getTrackingNumber())
                        .operationType(log.getOperationType())
                        .operator(log.getOperator())
                        .details(log.getDetails())
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private String generatePickupCode() {
        int todayCount = packageMapper.countToday();
        if (todayCount >= MAX_PER_LETTER * MAX_LETTERS) {
            throw new BusinessException(500, "当日入库已达上限");
        }
        char letter = (char) ('A' + todayCount / MAX_PER_LETTER);
        int serial = (todayCount % MAX_PER_LETTER) + 1;
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
                + "-" + letter + "-" + String.format("%03d", serial);
    }

    private PackageVO toVO(PackageEntity entity) {
        boolean overdue = PackageStatus.AWAITING_PICKUP.name().equals(entity.getStatus())
                && entity.getCheckInTime() != null
                && entity.getCheckInTime().plusHours(48).isBefore(LocalDateTime.now());

        return PackageVO.builder()
                .id(entity.getId())
                .trackingNumber(entity.getTrackingNumber())
                .recipientPhone(entity.getRecipientPhone())
                .courierCompany(entity.getCourierCompany())
                .shelfLocation(entity.getShelfLocation())
                .pickupCode(entity.getPickupCode())
                .status(entity.getStatus())
                .checkInTime(entity.getCheckInTime())
                .checkOutTime(entity.getCheckOutTime())
                .overdue(overdue)
                .build();
    }
}
