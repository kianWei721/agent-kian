package com.kian.kbagent.parser;

import com.kian.kbagent.model.ParsedDocument;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentParser {

    boolean supports(String fileType);

    ParsedDocument parse(Path filePath, String originalFileName) throws IOException;
}
