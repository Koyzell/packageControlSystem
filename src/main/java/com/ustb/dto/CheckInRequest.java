package com.ustb.dto;

import com.ustb.entity.PackageEntity;
import com.ustb.enums.PackageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckInRequest {

    @NotBlank(message = "运单号不能为空")
    private String trackingNumber;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String recipientPhone;

    @NotBlank(message = "快递公司不能为空")
    private String courierCompany;

    @NotBlank(message = "货架位置不能为空")
    private String shelfLocation;

    public PackageEntity toEntity() {
        return PackageEntity.builder()
                .trackingNumber(trackingNumber)
                .recipientPhone(recipientPhone)
                .courierCompany(courierCompany)
                .shelfLocation(shelfLocation)
                .status(PackageStatus.AWAITING_PICKUP.name())
                .build();
    }
}
