package com.meeting.meeting_room_system.dto;

import jakarta.validation.constraints.NotNull;

public class ApprovalDTO {

    @NotNull(message = "预约ID不能为空")
    private Long reservationId;

    @NotNull(message = "审批人ID不能为空")
    private Long approverId;

    private String remark;

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
