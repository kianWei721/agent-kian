package com.kian.kbagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Integer chunkSize = 700;
    private Integer chunkOverlap = 120;
    private Integer vectorRecallLimit = 30;
    private Integer keywordRecallLimit = 20;
    private Integer finalTopK = 8;
    private Double minScore = 0.35D;
}
