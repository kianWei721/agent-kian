package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatReferenceVO {

    private String documentName;
    private Integer chunkIndex;
    private String content;
    private Double score;
}
