package com.kian.kbagent.service;

import com.kian.kbagent.vo.DocumentUploadVO;
import com.kian.kbagent.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentUploadVO upload(MultipartFile file);

    void parseDocument(Long documentId);

    List<DocumentVO> listDocuments();
}
