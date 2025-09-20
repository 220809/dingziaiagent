package com.dingzk.dingziaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

//@Configuration
public class PgVectorStoreConfig {

    @Resource
    private RagDocumentLoader ragDocumentLoader;

    @Bean
    public VectorStore pgVectorStore(@Qualifier("pgsqlJdbcTemplate")JdbcTemplate jdbcTemplate,
                                        EmbeddingModel dashscopeEmbeddingModel) {
        PgVectorStore pgVectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .initializeSchema(true)
                .removeExistingVectorStoreTable(true)
                .build();

        List<Document> documentList = ragDocumentLoader.loadMarkdownDocuments();
        pgVectorStore.add(documentList);
        return pgVectorStore;
    }
}
