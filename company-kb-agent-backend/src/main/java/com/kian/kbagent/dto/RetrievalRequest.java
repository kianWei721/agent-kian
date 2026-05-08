package com.kian.kbagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetrievalRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    private Integer topK = 8;
}
