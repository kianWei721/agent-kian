package com.kian.kbagent.service;

import com.kian.kbagent.vo.RetrievalResponseVO;

public interface RetrievalService {

    RetrievalResponseVO retrieve(String question, Integer topK);
}
