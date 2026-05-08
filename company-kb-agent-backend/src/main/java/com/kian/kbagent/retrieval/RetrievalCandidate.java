package com.kian.kbagent.retrieval;

import lombok.Data;

@Data
public class RetrievalCandidate {

    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String title;
    private String content;
    private String sourceFileName;
    private Double score;
    private String retrievalType;
}
