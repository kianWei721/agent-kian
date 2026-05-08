package com.kian.kbagent.service.impl;

import com.kian.kbagent.retrieval.RetrievalCandidate;
import com.kian.kbagent.service.RerankService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PassThroughRerankServiceImpl implements RerankService {

    @Override
    public List<RetrievalCandidate> rerank(String question, List<RetrievalCandidate> candidates) {
        return candidates;
    }
}
