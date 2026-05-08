package com.kian.kbagent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RetrievalRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    @Min(value = 1, message = "topK 最小为 1")
    @Max(value = 20, message = "topK 最大为 20")
    private Integer topK = 8;
}
