package com.kian.kbagent.mapper;

import com.kian.kbagent.retrieval.RetrievalCandidate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DocumentChunkMapper {

    @Delete("DELETE FROM kb_document_chunk WHERE document_id = #{documentId}")
    int deleteByDocumentId(@Param("documentId") Long documentId);

    @Insert("""
            INSERT INTO kb_document_chunk
            (document_id, chunk_index, title, content, content_hash, char_count, embedding, source_file_name)
            VALUES
            (#{documentId}, #{chunkIndex}, #{title}, #{content}, #{contentHash}, #{charCount}, CAST(#{embeddingLiteral} AS vector), #{sourceFileName})
            """)
    int insertChunk(@Param("documentId") Long documentId,
                    @Param("chunkIndex") Integer chunkIndex,
                    @Param("title") String title,
                    @Param("content") String content,
                    @Param("contentHash") String contentHash,
                    @Param("charCount") Integer charCount,
                    @Param("embeddingLiteral") String embeddingLiteral,
                    @Param("sourceFileName") String sourceFileName);

    @Select("""
            SELECT id,
                   document_id AS documentId,
                   chunk_index AS chunkIndex,
                   title,
                   content,
                   source_file_name AS sourceFileName,
                   1 - (embedding <=> CAST(#{embeddingLiteral} AS vector)) AS score,
                   'vector' AS retrievalType
            FROM kb_document_chunk
            WHERE embedding IS NOT NULL
            ORDER BY embedding <=> CAST(#{embeddingLiteral} AS vector)
            LIMIT #{limit}
            """)
    List<RetrievalCandidate> searchByVector(@Param("embeddingLiteral") String embeddingLiteral,
                                            @Param("limit") Integer limit);

    @Select("""
            SELECT id,
                   document_id AS documentId,
                   chunk_index AS chunkIndex,
                   title,
                   content,
                   source_file_name AS sourceFileName,
                   ts_rank_cd(search_vector, plainto_tsquery('simple', #{keyword})) AS score,
                   'keyword' AS retrievalType
            FROM kb_document_chunk
            WHERE search_vector @@ plainto_tsquery('simple', #{keyword})
            ORDER BY score DESC, document_id DESC, chunk_index ASC
            LIMIT #{limit}
            """)
    List<RetrievalCandidate> searchByKeyword(@Param("keyword") String keyword,
                                             @Param("limit") Integer limit);
}
