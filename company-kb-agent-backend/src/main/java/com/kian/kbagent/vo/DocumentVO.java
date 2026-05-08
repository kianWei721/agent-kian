package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentVO {

    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String parseStatus;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
