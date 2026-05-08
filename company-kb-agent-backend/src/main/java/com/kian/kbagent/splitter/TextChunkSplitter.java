package com.kian.kbagent.splitter;

import com.kian.kbagent.config.RagProperties;
import com.kian.kbagent.model.TextChunk;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunkSplitter {

    private final RagProperties ragProperties;

    public TextChunkSplitter(RagProperties ragProperties) {
        this.ragProperties = ragProperties;
    }

    public List<TextChunk> split(String title, String content) {
        String normalized = normalize(content);
        List<String> paragraphs = splitParagraphs(normalized);
        List<TextChunk> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int chunkIndex = 1;

        for (String paragraph : paragraphs) {
            if (current.length() > 0 && current.length() + paragraph.length() + 1 > ragProperties.getChunkSize()) {
                chunks.add(buildChunk(chunkIndex++, title, current.toString()));
                current = new StringBuilder(overlapTail(current.toString()));
            }
            if (current.length() > 0) {
                current.append("\n");
            }
            current.append(paragraph);
        }

        if (current.length() > 0) {
            chunks.add(buildChunk(chunkIndex, title, current.toString()));
        }
        return chunks;
    }

    private List<String> splitParagraphs(String content) {
        String[] segments = content.split("\\n\\s*\\n");
        List<String> paragraphs = new ArrayList<>();
        for (String segment : segments) {
            if (StringUtils.hasText(segment)) {
                paragraphs.add(segment.trim());
            }
        }
        if (paragraphs.isEmpty() && StringUtils.hasText(content)) {
            paragraphs.add(content.trim());
        }
        return paragraphs;
    }

    private String normalize(String content) {
        return content == null ? "" : content.replace("\r\n", "\n").trim();
    }

    private TextChunk buildChunk(int chunkIndex, String title, String content) {
        return TextChunk.builder()
                .chunkIndex(chunkIndex)
                .title(title)
                .content(content.trim())
                .build();
    }

    private String overlapTail(String content) {
        int overlap = ragProperties.getChunkOverlap();
        if (content.length() <= overlap) {
            return content;
        }
        return content.substring(content.length() - overlap);
    }
}
