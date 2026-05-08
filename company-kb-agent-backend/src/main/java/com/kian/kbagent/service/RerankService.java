package com.kian.kbagent.service;

import com.kian.kbagent.retrieval.RetrievalCandidate;

import java.util.List;

public interface RerankService {

    List<RetrievalCandidate> rerank(String question, List<RetrievalCandidate> candidates);
}
