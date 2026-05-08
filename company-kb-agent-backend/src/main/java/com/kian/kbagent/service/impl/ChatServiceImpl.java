package com.kian.kbagent.service.impl;

import com.kian.kbagent.client.DashScopeClient;
import com.kian.kbagent.rag.PromptBuilder;
import com.kian.kbagent.service.ChatService;
import com.kian.kbagent.service.RetrievalService;
import com.kian.kbagent.vo.ChatAnswerVO;
import com.kian.kbagent.vo.ChatReferenceVO;
import com.kian.kbagent.vo.RetrievalChunkVO;
import com.kian.kbagent.vo.RetrievalResponseVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final RetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final DashScopeClient dashScopeClient;

    public ChatServiceImpl(RetrievalService retrievalService,
                           PromptBuilder promptBuilder,
                           DashScopeClient dashScopeClient) {
        this.retrievalService = retrievalService;
        this.promptBuilder = promptBuilder;
        this.dashScopeClient = dashScopeClient;
    }

    @Override
    public ChatAnswerVO ask(Long sessionId, String question) {
        RetrievalResponseVO retrievalResponse = retrievalService.retrieve(question, null);
        List<RetrievalChunkVO> chunks = retrievalResponse.getChunks();
        List<ChatReferenceVO> references = buildReferences(chunks);
        if (references.isEmpty()) {
            return ChatAnswerVO.builder()
                    .answer(PromptBuilder.NO_EVIDENCE_ANSWER)
                    .references(List.of())
                    .build();
        }

        String prompt = promptBuilder.buildChatPrompt(question, chunks);
        String answer = dashScopeClient.chat(prompt);
        return ChatAnswerVO.builder()
                .answer(appendReferences(answer, references))
                .references(references)
                .build();
    }

    private List<ChatReferenceVO> buildReferences(List<RetrievalChunkVO> chunks) {
        if (chunks == null) {
            return List.of();
        }
        return chunks.stream()
                .map(chunk -> ChatReferenceVO.builder()
                        .documentName(chunk.getSourceFileName())
                        .chunkIndex(chunk.getChunkIndex())
                        .content(summarize(chunk.getContent()))
                        .score(chunk.getScore())
                        .build())
                .toList();
    }

    private String appendReferences(String answer, List<ChatReferenceVO> references) {
        StringBuilder builder = new StringBuilder(answer == null ? "" : answer.trim());
        builder.append("\n\n引用来源：\n");
        for (int index = 0; index < references.size(); index++) {
            ChatReferenceVO reference = references.get(index);
            builder.append("[").append(index + 1).append("] ")
                    .append(reference.getDocumentName())
                    .append("#chunk-")
                    .append(reference.getChunkIndex())
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private String summarize(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 120 ? content : content.substring(0, 120) + "...";
    }
}
