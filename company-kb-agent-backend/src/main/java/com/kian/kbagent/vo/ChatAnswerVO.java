package com.kian.kbagent.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatAnswerVO {

    private String answer;
    private List<ChatReferenceVO> references;
}
