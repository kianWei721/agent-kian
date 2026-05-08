package com.kian.kbagent.rag;

import com.kian.kbagent.vo.RetrievalChunkVO;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class PromptBuilder {

    public static final String NO_EVIDENCE_ANSWER = "未在知识库中找到可靠依据，建议补充相关文档。";

    public String buildChatPrompt(String question, List<RetrievalChunkVO> chunks) {
        return """
                你是一个公司内部知识库助手。
                你只能根据【知识库上下文】回答问题。
                如果上下文中没有明确依据，请原样回答：%s
                不要编造不存在的项目、接口、路径、命令、负责人、时间和结论。
                回答要结构清晰。
                如果涉及技术问题，请按“结论、依据、操作建议、引用来源”组织。
                回答中必须引用上下文片段编号，例如 [1]、[2]。
                
                【用户问题】
                %s
                
                【知识库上下文】
                %s
                
                请严格基于以上内容回答。
                """.formatted(NO_EVIDENCE_ANSWER, question, buildContext(chunks));
    }

    public String buildContext(List<RetrievalChunkVO> chunks) {
        if (CollectionUtils.isEmpty(chunks)) {
            return "无可用知识库上下文。";
        }
        StringBuilder context = new StringBuilder();
        for (int index = 0; index < chunks.size(); index++) {
            RetrievalChunkVO chunk = chunks.get(index);
            context.append("[").append(index + 1).append("] ")
                    .append("文档: ").append(chunk.getSourceFileName())
                    .append(" | chunk: ").append(chunk.getChunkIndex());
            if (chunk.getTitle() != null && !chunk.getTitle().isBlank()) {
                context.append(" | 标题: ").append(chunk.getTitle());
            }
            context.append("\n")
                    .append(chunk.getContent())
                    .append("\n\n");
        }
        return context.toString().trim();
    }
}
