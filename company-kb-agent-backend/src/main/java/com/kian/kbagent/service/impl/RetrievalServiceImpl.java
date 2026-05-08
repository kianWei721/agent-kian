package com.kian.kbagent.service.impl;

import com.kian.kbagent.common.util.VectorUtils;
import com.kian.kbagent.config.RagProperties;
import com.kian.kbagent.mapper.DocumentChunkMapper;
import com.kian.kbagent.retrieval.RetrievalCandidate;
import com.kian.kbagent.service.EmbeddingService;
import com.kian.kbagent.service.RetrievalService;
import com.kian.kbagent.vo.RetrievalChunkVO;
import com.kian.kbagent.vo.RetrievalResponseVO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RetrievalServiceImpl implements RetrievalService {

    private final RagProperties ragProperties;
    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingService embeddingService;

    public RetrievalServiceImpl(RagProperties ragProperties,
                                DocumentChunkMapper documentChunkMapper,
                                EmbeddingService embeddingService) {
        this.ragProperties = ragProperties;
        this.documentChunkMapper = documentChunkMapper;
        this.embeddingService = embeddingService;
    }

    @Override
    public RetrievalResponseVO retrieve(String question, Integer topK) {
        int resolvedTopK = topK == null ? ragProperties.getFinalTopK() : Math.min(topK, 20);
        String queryVector = VectorUtils.toPgVectorLiteral(embeddingService.embedSingle(question));
        List<RetrievalCandidate> vectorResults = documentChunkMapper.searchByVector(queryVector, ragProperties.getVectorRecallLimit());
        List<RetrievalCandidate> keywordResults = documentChunkMapper.searchByKeyword(question, ragProperties.getKeywordRecallLimit());

        Map<String, RetrievalCandidate> merged = new LinkedHashMap<>();
        mergeCandidates(merged, vectorResults);
        mergeCandidates(merged, keywordResults);

        List<RetrievalChunkVO> chunks = merged.values().stream()
                .filter(this::isQualified)
                .sorted(Comparator.comparing(RetrievalCandidate::getScore, Comparator.nullsLast(Double::compareTo)).reversed())
                .limit(resolvedTopK)
                .map(candidate -> RetrievalChunkVO.builder()
                        .documentId(candidate.getDocumentId())
                        .chunkIndex(candidate.getChunkIndex())
                        .title(candidate.getTitle())
                        .content(candidate.getContent())
                        .sourceFileName(candidate.getSourceFileName())
                        .score(candidate.getScore())
                        .retrievalType(candidate.getRetrievalType())
                        .build())
                .toList();

        return RetrievalResponseVO.builder()
                .question(question)
                .total(chunks.size())
                .chunks(chunks)
                .build();
    }

    private void mergeCandidates(Map<String, RetrievalCandidate> merged, List<RetrievalCandidate> candidates) {
        for (RetrievalCandidate candidate : candidates == null ? List.<RetrievalCandidate>of() : candidates) {
            String key = candidate.getDocumentId() + "-" + candidate.getChunkIndex();
            RetrievalCandidate existing = merged.get(key);
            if (existing == null) {
                merged.put(key, candidate);
                continue;
            }
            existing.setScore(Math.max(nullSafe(existing.getScore()), nullSafe(candidate.getScore())));
            if (!java.util.Objects.equals(existing.getRetrievalType(), candidate.getRetrievalType())) {
                existing.setRetrievalType("hybrid");
            }
        }
    }

    private boolean isQualified(RetrievalCandidate candidate) {
        if (candidate == null) {
            return false;
        }
        if ("vector".equals(candidate.getRetrievalType())) {
            return nullSafe(candidate.getScore()) >= ragProperties.getMinScore();
        }
        return true;
    }

    private double nullSafe(Double score) {
        return score == null ? 0D : score;
    }
}
