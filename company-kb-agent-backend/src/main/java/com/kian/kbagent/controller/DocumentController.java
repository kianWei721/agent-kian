package com.kian.kbagent.controller;

import com.kian.kbagent.common.result.Result;
import com.kian.kbagent.service.DocumentService;
import com.kian.kbagent.vo.DocumentUploadVO;
import com.kian.kbagent.vo.DocumentVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public Result<DocumentUploadVO> upload(@RequestPart("file") MultipartFile file) {
        return Result.success(documentService.upload(file));
    }

    @PostMapping("/{id}/parse")
    public Result<Void> parse(@PathVariable Long id) {
        documentService.parseDocument(id);
        return Result.success();
    }

    @GetMapping
    public Result<List<DocumentVO>> list() {
        return Result.success(documentService.listDocuments());
    }
}
