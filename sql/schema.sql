CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS kb_document (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(32) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    storage_path VARCHAR(1024) NOT NULL,
    parse_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE kb_document IS '知识库文档表';
COMMENT ON COLUMN kb_document.file_name IS '原始文件名';
COMMENT ON COLUMN kb_document.file_type IS '文件类型，如 pdf/docx/txt/md';
COMMENT ON COLUMN kb_document.file_size IS '文件大小，单位字节';
COMMENT ON COLUMN kb_document.storage_path IS '文件存储路径';
COMMENT ON COLUMN kb_document.parse_status IS '解析状态：PENDING/PARSING/SUCCESS/FAILED';
COMMENT ON COLUMN kb_document.error_message IS '解析失败错误信息';

CREATE TABLE IF NOT EXISTS kb_document_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES kb_document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    title VARCHAR(500),
    content TEXT NOT NULL,
    content_hash CHAR(64) NOT NULL,
    char_count INT NOT NULL DEFAULT 0,
    embedding VECTOR(1024),
    source_file_name VARCHAR(255) NOT NULL,
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector('simple', coalesce(title, '') || ' ' || coalesce(content, '') || ' ' || coalesce(source_file_name, ''))
    ) STORED,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_kb_document_chunk_doc_idx UNIQUE (document_id, chunk_index),
    CONSTRAINT uk_kb_document_chunk_hash UNIQUE (document_id, content_hash)
);

COMMENT ON TABLE kb_document_chunk IS '知识库文档切片表';
COMMENT ON COLUMN kb_document_chunk.chunk_index IS '切片序号，从 1 开始';
COMMENT ON COLUMN kb_document_chunk.title IS '切片标题或所属章节';
COMMENT ON COLUMN kb_document_chunk.content IS '切片正文';
COMMENT ON COLUMN kb_document_chunk.content_hash IS '切片内容哈希，用于去重';
COMMENT ON COLUMN kb_document_chunk.char_count IS '字符数';
COMMENT ON COLUMN kb_document_chunk.embedding IS '1024 维向量';
COMMENT ON COLUMN kb_document_chunk.source_file_name IS '来源文件名';

CREATE TABLE IF NOT EXISTS kb_chat_session (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE kb_chat_session IS '知识库问答会话表';

CREATE TABLE IF NOT EXISTS kb_chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES kb_chat_session(id) ON DELETE CASCADE,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    references_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE kb_chat_message IS '知识库问答消息表';
COMMENT ON COLUMN kb_chat_message.role IS '消息角色：user/assistant/system';
COMMENT ON COLUMN kb_chat_message.references_json IS '回答引用来源 JSON 快照';

CREATE INDEX IF NOT EXISTS idx_kb_document_parse_status ON kb_document(parse_status);
CREATE INDEX IF NOT EXISTS idx_kb_document_created_at ON kb_document(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_kb_document_chunk_document_id ON kb_document_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_kb_document_chunk_created_at ON kb_document_chunk(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_kb_document_chunk_search_vector ON kb_document_chunk USING GIN(search_vector);

CREATE INDEX IF NOT EXISTS idx_kb_chat_session_updated_at ON kb_chat_session(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_kb_chat_message_session_id ON kb_chat_message(session_id, created_at);

CREATE INDEX IF NOT EXISTS idx_kb_document_chunk_embedding
    ON kb_document_chunk
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
