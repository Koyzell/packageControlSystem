package com.ustb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardVO {
    private long todayCheckIn;
    private long todayPickup;
    private long overdueCount;
    private long awaitingCount;
}
