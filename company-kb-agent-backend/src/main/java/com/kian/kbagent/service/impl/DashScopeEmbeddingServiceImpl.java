package com.kian.kbagent.service.impl;

import com.kian.kbagent.client.DashScopeClient;
import com.kian.kbagent.common.exception.BusinessException;
import com.kian.kbagent.service.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DashScopeEmbeddingServiceImpl implements EmbeddingService {

    private final DashScopeClient dashScopeClient;

    public DashScopeEmbeddingServiceImpl(DashScopeClient dashScopeClient) {
        this.dashScopeClient = dashScopeClient;
    }

    @Override
    public List<Double> embedSingle(String text) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException("待向量化文本不能为空");
        }
        return embedBatch(List.of(text)).get(0);
    }

    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            throw new BusinessException("待向量化文本列表不能为空");
        }
        List<List<Double>> vectors = dashScopeClient.embedTexts(texts);
        if (vectors.size() != texts.size()) {
            throw new BusinessException(500, "Embedding 返回数量与输入数量不一致");
        }
        return vectors;
    }
}
