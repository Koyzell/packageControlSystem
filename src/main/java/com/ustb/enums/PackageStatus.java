package com.ustb.enums;

public enum PackageStatus {
    AWAITING_PICKUP("待取件"),
    PICKED_UP("已取件");

    private final String displayName;

    PackageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
