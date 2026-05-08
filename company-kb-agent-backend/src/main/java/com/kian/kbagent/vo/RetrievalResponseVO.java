package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RetrievalResponseVO {

    private String question;
    private Integer total;
    private List<RetrievalChunkVO> chunks;
}
