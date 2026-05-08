package com.kian.kbagent.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.kian.kbagent.common.exception.BusinessException;
import com.kian.kbagent.config.DashScopeProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DashScopeClient {

    private static final String EMBEDDING_URI = "/api/v1/services/embeddings/text-embedding/text-embedding";
    private static final String CHAT_URI = "/compatible-mode/v1/chat/completions";

    private final RestClient restClient;
    private final DashScopeProperties properties;

    public DashScopeClient(RestClient restClient, DashScopeProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public List<List<Double>> embedTexts(List<String> texts) {
        if (CollectionUtils.isEmpty(texts)) {
            return List.of();
        }
        validateApiKey();

        Map<String, Object> requestBody = Map.of(
                "model", properties.getEmbeddingModel(),
                "input", Map.of("texts", texts)
        );

        JsonNode response = restClient.post()
                .uri(properties.getBaseUrl() + EMBEDDING_URI)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        JsonNode embeddingsNode = response == null ? null : response.path("output").path("embeddings");
        if (embeddingsNode == null || !embeddingsNode.isArray() || embeddingsNode.isEmpty()) {
            throw new BusinessException(500, "DashScope Embedding 响应为空");
        }

        List<List<Double>> result = new ArrayList<>();
        for (JsonNode embeddingNode : embeddingsNode) {
            JsonNode vectorNode = embeddingNode.path("embedding");
            if (!vectorNode.isArray()) {
                throw new BusinessException(500, "DashScope Embedding 数据格式错误");
            }
            List<Double> vector = new ArrayList<>(vectorNode.size());
            for (JsonNode valueNode : vectorNode) {
                vector.add(valueNode.asDouble());
            }
            result.add(vector);
        }
        return result;
    }

    public String chat(String prompt) {
        validateApiKey();
        Map<String, Object> requestBody = Map.of(
                "model", properties.getChatModel(),
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        JsonNode response = restClient.post()
                .uri(properties.getBaseUrl() + CHAT_URI)
                .header("Authorization", "Bearer " + properties.getApiKey())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        JsonNode contentNode = response == null ? null
                : response.path("choices").path(0).path("message").path("content");
        if (contentNode == null || contentNode.isMissingNode() || !contentNode.isTextual()) {
            throw new BusinessException(500, "DashScope Chat 响应为空");
        }
        return contentNode.asText();
    }

    public String getReservedRerankModel() {
        return properties.getRerankModel();
    }

    private void validateApiKey() {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException(500, "未配置 DASHSCOPE_API_KEY");
        }
    }
}
