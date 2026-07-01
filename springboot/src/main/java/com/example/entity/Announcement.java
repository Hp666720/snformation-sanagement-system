package com.example.entity;


import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告信息实体类
 * 存储公告的所有字段信息，包括标题、内容、发布人和状态等
 */
@Data
public class Announcement {

    private Integer id;

    private String title;

    private String content;

    private Integer publisherId;

    private String publisherName;

    private LocalDateTime publishTime;

    private String status;
}
