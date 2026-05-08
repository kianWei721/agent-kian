package com.kian.kbagent.service.impl;

import com.kian.kbagent.common.util.VectorUtils;
import com.kian.kbagent.config.RagProperties;
import com.kian.kbagent.mapper.DocumentChunkMapper;
import com.kian.kbagent.retrieval.RetrievalCandidate;
import com.kian.kbagent.service.EmbeddingService;
import com.kian.kbagent.service.RerankService;
import com.kian.kbagent.service.RetrievalService;
import com.kian.kbagent.vo.RetrievalChunkVO;
import com.kian.kbagent.vo.RetrievalResponseVO;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RetrievalServiceImpl implements RetrievalService {

    private final RagProperties ragProperties;
    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingService embeddingService;
    private final RerankService rerankService;

    public RetrievalServiceImpl(RagProperties ragProperties,
                                DocumentChunkMapper documentChunkMapper,
                                EmbeddingService embeddingService,
                                RerankService rerankService) {
        this.ragProperties = ragProperties;
        this.documentChunkMapper = documentChunkMapper;
        this.embeddingService = embeddingService;
        this.rerankService = rerankService;
    }

    @Override
    public RetrievalResponseVO retrieve(String question, Integer topK) {
        int resolvedTopK = resolveTopK(topK);
        String queryVector = VectorUtils.toPgVectorLiteral(embeddingService.embedSingle(question));
        List<RetrievalCandidate> vectorResults = documentChunkMapper.searchByVector(queryVector, ragProperties.getVectorRecallLimit());
        List<RetrievalCandidate> keywordResults = documentChunkMapper.searchByKeyword(question, ragProperties.getKeywordRecallLimit());

        Map<String, RetrievalCandidate> merged = new LinkedHashMap<>();
        mergeVectorCandidates(merged, vectorResults);
        mergeKeywordCandidates(merged, keywordResults);

        List<RetrievalCandidate> candidates = merged.values().stream()
                .peek(this::fillFinalScore)
                .filter(this::isQualified)
                .sorted(Comparator.comparing(RetrievalCandidate::getFinalScore, Comparator.nullsLast(Double::compareTo)).reversed())
                .toList();

        if (Boolean.TRUE.equals(ragProperties.getRerankEnabled())) {
            candidates = rerankService.rerank(question, candidates);
        }

        List<RetrievalChunkVO> chunks = candidates.stream()
                .limit(resolvedTopK)
                .map(candidate -> RetrievalChunkVO.builder()
                        .documentId(candidate.getDocumentId())
                        .chunkIndex(candidate.getChunkIndex())
                        .title(candidate.getTitle())
                        .content(candidate.getContent())
                        .sourceFileName(candidate.getSourceFileName())
                        .score(candidate.getFinalScore())
                        .retrievalType(candidate.getRetrievalType())
                        .build())
                .toList();

        return RetrievalResponseVO.builder()
                .question(question)
                .total(chunks.size())
                .chunks(chunks)
                .build();
    }

    private int resolveTopK(Integer topK) {
        int fallback = ragProperties.getFinalTopK() == null ? ragProperties.getFinalTopKMax() : ragProperties.getFinalTopK();
        int value = topK == null ? fallback : topK;
        int min = ragProperties.getFinalTopKMin() == null ? 5 : ragProperties.getFinalTopKMin();
        int max = ragProperties.getFinalTopKMax() == null ? 8 : ragProperties.getFinalTopKMax();
        return Math.max(min, Math.min(value, max));
    }

    private void mergeVectorCandidates(Map<String, RetrievalCandidate> merged, List<RetrievalCandidate> candidates) {
        for (RetrievalCandidate candidate : candidates == null ? List.<RetrievalCandidate>of() : candidates) {
            candidate.setVectorScore(nullSafe(candidate.getScore()));
            candidate.setRetrievalType("vector");
            merged.put(buildKey(candidate), candidate);
        }
    }

    private void mergeKeywordCandidates(Map<String, RetrievalCandidate> merged, List<RetrievalCandidate> candidates) {
        for (RetrievalCandidate candidate : candidates == null ? List.<RetrievalCandidate>of() : candidates) {
            candidate.setKeywordScore(nullSafe(candidate.getScore()));
            String key = buildKey(candidate);
            RetrievalCandidate existing = merged.get(key);
            if (existing == null) {
                candidate.setRetrievalType("keyword");
                merged.put(key, candidate);
                continue;
            }
            existing.setKeywordScore(Math.max(nullSafe(existing.getKeywordScore()), nullSafe(candidate.getKeywordScore())));
            if (!Objects.equals(existing.getRetrievalType(), candidate.getRetrievalType())) {
                existing.setRetrievalType("hybrid");
            }
        }
    }

    private void fillFinalScore(RetrievalCandidate candidate) {
        double vectorScore = nullSafe(candidate.getVectorScore());
        double keywordScore = nullSafe(candidate.getKeywordScore());
        double normalizedKeyword = Math.min(keywordScore, 1D);
        double hybridBonus = "hybrid".equals(candidate.getRetrievalType()) ? 0.15D : 0D;
        candidate.setFinalScore(vectorScore * 0.7D + normalizedKeyword * 0.3D + hybridBonus);
    }

    private boolean isQualified(RetrievalCandidate candidate) {
        double vectorScore = nullSafe(candidate.getVectorScore());
        double keywordScore = nullSafe(candidate.getKeywordScore());
        if ("keyword".equals(candidate.getRetrievalType())) {
            return keywordScore > 0D;
        }
        if ("hybrid".equals(candidate.getRetrievalType())) {
            return vectorScore >= ragProperties.getMinScore() || keywordScore > 0D;
        }
        return vectorScore >= ragProperties.getMinScore();
    }

    private String buildKey(RetrievalCandidate candidate) {
        return candidate.getDocumentId() + "-" + candidate.getChunkIndex();
    }

    private double nullSafe(Double score) {
        return score == null ? 0D : score;
    }
}
