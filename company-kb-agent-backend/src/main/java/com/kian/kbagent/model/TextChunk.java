package com.kian.kbagent.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextChunk {

    private Integer chunkIndex;
    private String title;
    private String content;
}
