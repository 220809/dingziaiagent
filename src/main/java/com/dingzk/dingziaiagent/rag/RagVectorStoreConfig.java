package com.dingzk.dingziaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RagVectorStoreConfig {

    @Resource
    private RagDocumentLoader ragDocumentLoader;

    @Bean
    public VectorStore appVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();

        List<Document> documentList = ragDocumentLoader.loadMarkdownDocuments();
        simpleVectorStore.add(documentList);
        return simpleVectorStore;
    }
}
