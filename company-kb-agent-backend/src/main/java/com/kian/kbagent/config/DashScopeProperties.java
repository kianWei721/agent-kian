package com.kian.kbagent.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    @NotBlank
    private String apiKey;
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String embeddingModel;
}
