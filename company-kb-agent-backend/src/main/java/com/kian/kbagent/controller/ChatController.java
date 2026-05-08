package com.kian.kbagent.controller;

import com.kian.kbagent.common.result.Result;
import com.kian.kbagent.dto.ChatAskRequest;
import com.kian.kbagent.service.ChatService;
import com.kian.kbagent.vo.ChatAnswerVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public Result<ChatAnswerVO> ask(@Valid @RequestBody ChatAskRequest request) {
        return Result.success(chatService.ask(request.getSessionId(), request.getQuestion()));
    }
}
