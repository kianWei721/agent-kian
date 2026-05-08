package com.kian.kbagent.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    private String apiKey;
    @NotBlank
    private String baseUrl;
    @NotBlank
    private String embeddingModel;
}
