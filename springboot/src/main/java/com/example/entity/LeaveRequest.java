package com.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 请假申请信息实体类
 * 存储请假申请的所有字段信息，包括申请人、审核人、时间戳和审核状态等
 */
@Data
public class LeaveRequest {

    private Integer id;

    private String title;

    private String description;

    private Integer requesterId;

    private String requesterName;

    private LocalDateTime submitTime;

    private Integer reviewerId;

    private String reviewerName;

    private String status;

    private String reviewComment;

    private LocalDateTime reviewTime;
}
