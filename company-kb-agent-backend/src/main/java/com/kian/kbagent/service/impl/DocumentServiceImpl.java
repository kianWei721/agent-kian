package com.kian.kbagent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kian.kbagent.common.exception.BusinessException;
import com.kian.kbagent.common.util.FileStorageUtils;
import com.kian.kbagent.common.util.HashUtils;
import com.kian.kbagent.common.util.VectorUtils;
import com.kian.kbagent.config.StorageProperties;
import com.kian.kbagent.entity.KbDocument;
import com.kian.kbagent.enums.DocumentParseStatus;
import com.kian.kbagent.mapper.DocumentChunkMapper;
import com.kian.kbagent.mapper.DocumentMapper;
import com.kian.kbagent.model.ParsedDocument;
import com.kian.kbagent.model.TextChunk;
import com.kian.kbagent.parser.DocumentParserFactory;
import com.kian.kbagent.service.DocumentService;
import com.kian.kbagent.service.EmbeddingService;
import com.kian.kbagent.splitter.TextChunkSplitter;
import com.kian.kbagent.vo.DocumentUploadVO;
import com.kian.kbagent.vo.DocumentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final StorageProperties storageProperties;
    private final DocumentMapper documentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final DocumentParserFactory documentParserFactory;
    private final TextChunkSplitter textChunkSplitter;
    private final EmbeddingService embeddingService;
    private final TransactionTemplate transactionTemplate;

    public DocumentServiceImpl(StorageProperties storageProperties,
                               DocumentMapper documentMapper,
                               DocumentChunkMapper documentChunkMapper,
                               DocumentParserFactory documentParserFactory,
                               TextChunkSplitter textChunkSplitter,
                               EmbeddingService embeddingService,
                               TransactionTemplate transactionTemplate) {
        this.storageProperties = storageProperties;
        this.documentMapper = documentMapper;
        this.documentChunkMapper = documentChunkMapper;
        this.documentParserFactory = documentParserFactory;
        this.textChunkSplitter = textChunkSplitter;
        this.embeddingService = embeddingService;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public DocumentUploadVO upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFileName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : file.getName();
        String fileType = FileStorageUtils.resolveExtension(originalFileName);
        documentParserFactory.getParser(fileType);

        Path storagePath = FileStorageUtils.buildStoragePath(storageProperties.getUploadDir(), originalFileName);
        try {
            Files.createDirectories(storagePath.getParent());
            file.transferTo(storagePath);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件保存失败: " + ex.getMessage());
        }

        KbDocument document = new KbDocument();
        document.setFileName(originalFileName);
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setStoragePath(storagePath.toString());
        document.setParseStatus(DocumentParseStatus.PENDING.name());
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.insert(document);

        return DocumentUploadVO.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .parseStatus(document.getParseStatus())
                .build();
    }

    @Override
    public void parseDocument(Long documentId) {
        KbDocument document = requireDocument(documentId);
        updateParseStatus(document, DocumentParseStatus.PARSING, null);

        try {
            transactionTemplate.executeWithoutResult(status -> doParseAndPersist(document));
            updateParseStatus(document, DocumentParseStatus.SUCCESS, null);
        } catch (Exception ex) {
            String message = ex instanceof BusinessException ? ex.getMessage() : "文档解析失败: " + ex.getMessage();
            log.error("Parse document failed, documentId={}", documentId, ex);
            updateParseStatus(document, DocumentParseStatus.FAILED, truncate(message, 1000));
            if (ex instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException(500, message);
        }
    }

    @Override
    public List<DocumentVO> listDocuments() {
        return documentMapper.selectList(new LambdaQueryWrapper<KbDocument>()
                        .orderByDesc(KbDocument::getCreatedAt))
                .stream()
                .map(document -> DocumentVO.builder()
                        .id(document.getId())
                        .fileName(document.getFileName())
                        .fileType(document.getFileType())
                        .fileSize(document.getFileSize())
                        .parseStatus(document.getParseStatus())
                        .errorMessage(document.getErrorMessage())
                        .createdAt(document.getCreatedAt())
                        .updatedAt(document.getUpdatedAt())
                        .build())
                .toList();
    }

    private void doParseAndPersist(KbDocument document) {
        ParsedDocument parsedDocument;
        try {
            parsedDocument = documentParserFactory.getParser(document.getFileType())
                    .parse(Path.of(document.getStoragePath()), document.getFileName());
        } catch (IOException ex) {
            throw new BusinessException(500, "文档解析失败: " + ex.getMessage());
        }

        if (!StringUtils.hasText(parsedDocument.getContent())) {
            throw new BusinessException("文档解析结果为空");
        }

        List<TextChunk> chunks = textChunkSplitter.split(parsedDocument.getTitle(), parsedDocument.getContent());
        if (chunks.isEmpty()) {
            throw new BusinessException("文档切片结果为空");
        }

        List<List<Double>> embeddings = embeddingService.embedBatch(chunks.stream().map(TextChunk::getContent).toList());
        documentChunkMapper.deleteByDocumentId(document.getId());
        for (int index = 0; index < chunks.size(); index++) {
            TextChunk chunk = chunks.get(index);
            documentChunkMapper.insertChunk(
                    document.getId(),
                    chunk.getChunkIndex(),
                    chunk.getTitle(),
                    chunk.getContent(),
                    HashUtils.sha256(chunk.getTitle() + "\n" + chunk.getContent()),
                    chunk.getContent().length(),
                    VectorUtils.toPgVectorLiteral(embeddings.get(index)),
                    document.getFileName()
            );
        }
    }

    private KbDocument requireDocument(Long documentId) {
        KbDocument document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(404, "文档不存在");
        }
        return document;
    }

    private void updateParseStatus(KbDocument document, DocumentParseStatus status, String errorMessage) {
        KbDocument update = new KbDocument();
        update.setId(document.getId());
        update.setParseStatus(status.name());
        update.setErrorMessage(errorMessage);
        update.setUpdatedAt(LocalDateTime.now());
        documentMapper.updateById(update);
        document.setParseStatus(status.name());
        document.setErrorMessage(errorMessage);
        document.setUpdatedAt(update.getUpdatedAt());
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
