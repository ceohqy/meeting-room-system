package com.meeting.meeting_room_system.enums;

public enum ReservationStatus {
    PENDING("待审批"),
    APPROVED("已通过"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
