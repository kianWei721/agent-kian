package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentUploadVO {

    private Long id;
    private String fileName;
    private String fileType;
    private String parseStatus;
}
