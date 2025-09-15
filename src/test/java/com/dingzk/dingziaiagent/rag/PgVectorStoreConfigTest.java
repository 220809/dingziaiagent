package com.dingzk.dingziaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorStore;

    @Test
    void testPgVectorStore() {
        List<Document> documents = List.of(
                new Document("Java 语言是面向对象语言之一，具有面向对象的诸多特性。", Map.of("meta1", "meta1")),
                new Document("C 语言真的好难学！！"),
                new Document("Java 相对于 C/C++ 而言，真的更容易上手学习吗？", Map.of("meta2", "meta2")));

        // Add the documents to PGVector
        pgVectorStore.add(documents);

        // Retrieve documents similar to a query
        List<Document> results = pgVectorStore.similaritySearch(SearchRequest.builder().query("Java").topK(3).build());

        Assertions.assertNotNull(results);
    }
}