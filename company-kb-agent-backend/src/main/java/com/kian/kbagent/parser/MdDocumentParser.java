package com.kian.kbagent.parser;

import com.kian.kbagent.model.ParsedDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MdDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "md".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath, String originalFileName) throws IOException {
        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        String title = content.lines()
                .filter(line -> line.startsWith("#"))
                .findFirst()
                .map(line -> line.replaceFirst("^#+\\s*", "").trim())
                .filter(value -> !value.isEmpty())
                .orElse(originalFileName);
        return ParsedDocument.builder()
                .title(title)
                .content(content)
                .build();
    }
}
