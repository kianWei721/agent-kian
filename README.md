# 公司知识库 Agent

一个面向企业内部知识沉淀与智能问答的 RAG 项目，目标是把项目文档、接口文档、部署文档、Bug 修复记录、日报周报、技术方案等资料统一接入知识库，完成自动解析、切片、向量化、入库与问答检索，减少人工翻阅多个文档的成本。

## 1. 项目介绍

本项目采用前后端分离架构：

- 后端：Spring Boot 3.x + Java 17 + PostgreSQL + pgvector
- 前端：Vue 3 + Vite + Element Plus
- 大模型：阿里云百炼 / DashScope
- 检索策略：向量召回 + 关键词召回 + 去重 + 阈值过滤 + 预留重排序扩展

项目优先保证：

- 能完整跑通上传、解析、入库、检索、问答闭环
- 回答必须带引用来源
- 没有可靠依据时明确拒答
- 架构清晰，方便后续扩展 rerank、SSE、权限、多知识库等能力

## 2. 技术栈

### 后端

- Java 17
- Spring Boot 3.x
- Spring Web
- Spring Validation
- MyBatis Plus
- PostgreSQL 15+
- pgvector
- Lombok
- Hutool（可选，用于工具类）
- PDFBox（PDF 解析）
- Apache POI（DOCX 解析）
- Jackson（JSON 处理）
- SLF4J + Logback（日志）

### 前端

- Vue 3
- Vite
- Element Plus
- Axios
- Vue Router

### 模型与 AI 能力

- 对话模型：`qwen-plus`
- Embedding 模型：`text-embedding-v4`
- Embedding 维度：`1024`
- Rerank 模型：`qwen3-rerank`（第一阶段预留接口，第二阶段接入）

## 3. 功能模块

### 3.1 文档管理模块

- 上传文档：支持 `pdf`、`docx`、`txt`、`md`
- 查看文档列表
- 删除文档
- 查看解析状态：待解析、解析中、成功、失败
- 重新解析文档
- 上传后自动执行：
  - 原文件存储
  - 文本解析
  - 文档切片
  - Embedding 生成
  - 向量入库

### 3.2 知识切片模块

- 按文档标题、段落、空行等语义边界进行切片
- 中文场景下建议：
  - chunk 大小：500 ~ 800 字
  - overlap：100 ~ 150 字
- 每个 chunk 至少保存：
  - `document_id`
  - `chunk_index`
  - `title`
  - `content`
  - `content_hash`
  - `embedding`
  - `source_file_name`
  - `created_at`

### 3.3 检索模块

混合检索流程：

1. 对用户问题做 Embedding
2. 使用 pgvector 做向量召回 Top 30
3. 使用 PostgreSQL 全文检索做关键词召回 Top 20
4. 合并结果并按 `document_id + chunk_index` 去重
5. 应用最小相似度阈值，降低噪声
6. 预留 rerank 接口，对候选结果做重排序
7. 最终选择 Top 5 ~ Top 8 作为上下文

### 3.4 Agent 问答模块

- 接收用户问题
- 触发混合检索
- 基于检索结果构建 Prompt
- 调用 `qwen-plus`
- 返回答案与引用来源
- 若无可靠上下文，直接返回：
  - `未在知识库中找到可靠依据，建议补充相关文档。`

### 3.5 前端页面

- 文档上传页
- 文档列表页
- 知识库问答页
- 默认单用户模式，第一阶段不接登录

## 4. 项目目录设计

### 4.1 后端目录

```text
company-kb-agent-backend
├── pom.xml
├── src/main/java/com/kian/kbagent
│   ├── KbAgentApplication.java
│   ├── common
│   │   ├── exception
│   │   ├── result
│   │   └── util
│   ├── config
│   ├── client
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── parser
│   ├── rag
│   ├── retrieval
│   ├── service
│   ├── service/impl
│   ├── splitter
│   └── vo
└── src/main/resources
    ├── application.yml
    └── mapper
```

### 4.2 前端目录

```text
company-kb-agent-web
├── package.json
├── vite.config.js
└── src
    ├── api
    ├── components
    ├── router
    ├── utils
    └── views
```

## 5. RAG 流程设计

### 5.1 文档入库流程

1. 用户上传文档
2. 系统保存原始文件到本地存储目录
3. 根据文件类型选择解析器：
   - PDF → PDFBox
   - DOCX → Apache POI
   - TXT / MD → 直接读取
4. 提取正文内容和标题信息
5. 按语义规则切片
6. 对每个 chunk 调用 DashScope Embedding
7. 将 chunk、Embedding、元数据写入 PostgreSQL
8. 更新文档解析状态

### 5.2 问答流程

1. 用户提交问题
2. 对问题生成 Embedding
3. 执行向量召回
4. 执行关键词召回
5. 合并去重并应用阈值过滤
6. 可选 rerank
7. 选取最终上下文
8. 使用 PromptBuilder 构造约束型 Prompt
9. 调用 `qwen-plus`
10. 返回答案和引用来源

### 5.3 召回质量策略

为保证召回质量，第一阶段必须实现以下策略：

- 切片大小控制在 500 ~ 800 字
- 采用 overlap 保持上下文连续性
- chunk 保存标题、文件名、chunk 序号
- 混合召回，不只依赖向量搜索
- 设置最小相似度阈值
- 最终上下文限制在 5 ~ 8 个 chunk
- Prompt 严格约束模型只能依据上下文回答
- 无可靠上下文时明确拒答
- 所有回答必须附带来源
- 预留 rerank 扩展点

## 6. 数据库设计

数据库初始化 SQL 文件：`/sql/schema.sql`

### 6.1 表清单

1. `kb_document`：文档元信息
2. `kb_document_chunk`：切片内容与向量
3. `kb_chat_session`：问答会话
4. `kb_chat_message`：会话消息

### 6.2 关键设计说明

- 启用 `pgvector` 扩展，存储 `vector(1024)`
- 为切片内容建立全文检索列与索引
- 为向量列建立 ivfflat 索引
- 删除文档时，级联删除关联 chunk
- `content_hash` 用于避免重复切片
- `references_json` 存储问答引用来源快照

### 6.3 建表 SQL 摘要

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS kb_document (...);
CREATE TABLE IF NOT EXISTS kb_document_chunk (... embedding vector(1024) ...);
CREATE TABLE IF NOT EXISTS kb_chat_session (...);
CREATE TABLE IF NOT EXISTS kb_chat_message (...);
```

完整 SQL 见：

- `/home/runner/work/agent-kian/agent-kian/sql/schema.sql`

## 7. 接口设计

### 7.1 文档接口

#### 上传文档

- `POST /api/documents/upload`
- `Content-Type: multipart/form-data`

#### 查询文档列表

- `GET /api/documents`

#### 删除文档

- `DELETE /api/documents/{id}`

#### 重新解析文档

- `POST /api/documents/{id}/reparse`

### 7.2 问答接口

#### 普通知识库问答

- `POST /api/chat/ask`

请求示例：

```json
{
  "question": "订单服务部署失败如何排查？",
  "sessionId": 1
}
```

返回示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "xxx",
    "references": [
      {
        "documentName": "xxx.pdf",
        "chunkIndex": 3,
        "content": "引用片段摘要"
      }
    ]
  }
}
```

#### 查看会话列表

- `GET /api/chat/sessions`

#### 查看会话消息

- `GET /api/chat/sessions/{id}/messages`

## 8. Prompt 设计

后端提供 `PromptBuilder`，统一拼装 RAG Prompt：

```text
你是一个公司内部知识库助手。
你只能根据【知识库上下文】回答问题。
如果上下文中没有明确依据，请回答：未在知识库中找到可靠依据，建议补充相关文档。
不要编造不存在的项目、接口、路径、命令、负责人、时间和结论。
回答要结构清晰。
如果涉及技术问题，请按“结论、依据、操作建议、引用来源”组织。

【用户问题】
{question}

【知识库上下文】
{context}

请基于以上内容回答。
```

## 9. 环境变量与配置

### 9.1 必填环境变量

- `DASHSCOPE_API_KEY`

### 9.2 推荐配置项

```yaml
dashscope:
  api-key: ${DASHSCOPE_API_KEY:}
  chat-model: ${DASHSCOPE_CHAT_MODEL:qwen-plus}
  embedding-model: ${DASHSCOPE_EMBEDDING_MODEL:text-embedding-v4}
  rerank-model: ${DASHSCOPE_RERANK_MODEL:qwen3-rerank}
```

### 9.3 数据库配置示例

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/company_kb_agent
    username: postgres
    password: postgres
```

## 10. 启动方式

### 10.1 本地依赖

- JDK 17
- Node.js 18+
- PostgreSQL 15+
- pgvector 扩展

### 10.2 启动流程

1. 启动 PostgreSQL
2. 安装 pgvector
3. 创建数据库 `company_kb_agent`
4. 执行 `/sql/schema.sql`
5. 配置 `DASHSCOPE_API_KEY`
6. 启动后端
7. 启动前端
8. 上传测试文档
9. 发起问答验证召回与引用效果

## 11. 文件创建清单（第一步）

本次先完成以下文件：

1. `README.md`
   - 位置：`/home/runner/work/agent-kian/agent-kian/README.md`
   - 内容：项目设计文档，包含架构、流程、数据库、接口、配置、启动说明
2. `schema.sql`
   - 位置：`/home/runner/work/agent-kian/agent-kian/sql/schema.sql`
   - 内容：PostgreSQL + pgvector 初始化脚本，可直接执行

## 12. 后续优化方向

- 接入 `qwen3-rerank` 提升候选排序质量
- 支持异步解析任务队列
- 支持 SSE 流式输出
- 支持多知识库、标签、目录隔离
- 支持 OCR 扫描件解析
- 支持 Excel / PPT / 图片文档
- 支持权限体系与审计日志
- 支持文档版本管理
- 支持分段摘要与 chunk 标题增强
- 支持召回评估与离线调参

## 13. 当前阶段说明

当前仓库已完成第一步中的两项基础产物：

- 项目 README 设计文档
- PostgreSQL 建表 SQL

下一步建议进入第二步，实现 Spring Boot 后端工程、配置文件、实体、Mapper、文档解析、检索与问答主流程。
