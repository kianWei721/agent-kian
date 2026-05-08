package com.kian.kbagent.parser;

import com.kian.kbagent.model.ParsedDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TxtDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String fileType) {
        return "txt".equalsIgnoreCase(fileType);
    }

    @Override
    public ParsedDocument parse(Path filePath, String originalFileName) throws IOException {
        return ParsedDocument.builder()
                .title(originalFileName)
                .content(Files.readString(filePath, StandardCharsets.UTF_8))
                .build();
    }
}
