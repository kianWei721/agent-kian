package com.kian.kbagent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("kb_document_chunk")
public class KbDocumentChunk {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String title;
    private String content;
    private String contentHash;
    private Integer charCount;
    private String embedding;
    private String sourceFileName;
    private LocalDateTime createdAt;
}
