package com.kian.kbagent.controller;

import com.kian.kbagent.common.result.Result;
import com.kian.kbagent.dto.RetrievalRequest;
import com.kian.kbagent.service.RetrievalService;
import com.kian.kbagent.vo.RetrievalResponseVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retrieval")
public class RetrievalController {

    private final RetrievalService retrievalService;

    public RetrievalController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @PostMapping("/search")
    public Result<RetrievalResponseVO> search(@Valid @RequestBody RetrievalRequest request) {
        return Result.success(retrievalService.retrieve(request.getQuestion(), request.getTopK()));
    }
}
