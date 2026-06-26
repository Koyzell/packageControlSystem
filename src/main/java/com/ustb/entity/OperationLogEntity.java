package com.ustb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogEntity {
    private Long id;
    private Long packageId;
    private String trackingNumber;
    private String operationType;
    private String operator;
    private String details;
    private LocalDateTime createdAt;
}
