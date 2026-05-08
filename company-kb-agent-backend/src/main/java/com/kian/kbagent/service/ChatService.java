package com.kian.kbagent.service;

import com.kian.kbagent.vo.ChatAnswerVO;

public interface ChatService {

    ChatAnswerVO ask(Long sessionId, String question);
}
