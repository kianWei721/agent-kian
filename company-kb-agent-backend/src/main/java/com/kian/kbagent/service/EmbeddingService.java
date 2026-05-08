package com.kian.kbagent.service;

import java.util.List;

public interface EmbeddingService {

    List<Double> embedSingle(String text);

    List<List<Double>> embedBatch(List<String> texts);
}
