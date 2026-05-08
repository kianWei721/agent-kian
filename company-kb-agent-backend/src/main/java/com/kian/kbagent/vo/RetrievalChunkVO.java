package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RetrievalChunkVO {

    private Long documentId;
    private Integer chunkIndex;
    private String title;
    private String content;
    private String sourceFileName;
    private Double score;
    private String retrievalType;
}
