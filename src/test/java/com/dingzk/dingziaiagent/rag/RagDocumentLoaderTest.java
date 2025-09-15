package com.dingzk.dingziaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RagDocumentLoaderTest {

    @Resource
    private RagDocumentLoader ragDocumentLoader;

    @Test
    void testLoadMarkdownDocuments() {
        ragDocumentLoader.loadMarkdownDocuments();
    }
}