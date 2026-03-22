package com.meeting.meeting_room_system.controller;

import com.meeting.meeting_room_system.annotation.RequirePermission;
import com.meeting.meeting_room_system.common.Result;
import com.meeting.meeting_room_system.dto.ApprovalDTO;
import com.meeting.meeting_room_system.entity.ApprovalRecord;
import com.meeting.meeting_room_system.service.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @GetMapping("/list")
    public Result<List<com.meeting.meeting_room_system.dto.ApprovalListDTO>> list(HttpServletRequest request){
        String role = (String) request.getAttribute("currentRole");
        Long tenantId = (Long) request.getAttribute("currentTenantId");
        // 超级管理员看全部，其他角色只看本租户
        if ("SUPER_ADMIN".equals(role)) {
            return Result.success(approvalService.listAll());
        }
        return Result.success(approvalService.listByTenantId(tenantId));
    }

    @PostMapping("/pass")
    @RequirePermission("reservation:approve")
    public Result<ApprovalRecord> pass(@Valid @RequestBody ApprovalDTO dto){
        ApprovalRecord record = new ApprovalRecord();
        record.setReservationId(dto.getReservationId());
        record.setApproverId(dto.getApproverId());
        record.setRemark(dto.getRemark());
        return Result.success("审批通过", approvalService.approve(record));
    }

    @PostMapping("/reject")
    @RequirePermission("reservation:approve")
    public Result<ApprovalRecord> reject(@Valid @RequestBody ApprovalDTO dto){
        ApprovalRecord record = new ApprovalRecord();
        record.setReservationId(dto.getReservationId());
        record.setApproverId(dto.getApproverId());
        record.setRemark(dto.getRemark());
        return Result.success("审批拒绝", approvalService.reject(record));
    }
}