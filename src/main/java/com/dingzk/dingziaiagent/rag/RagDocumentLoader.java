package com.dingzk.dingziaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 读取 RAG 知识库文档
 */
@Component
@Slf4j
public class RagDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public RagDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver= resourcePatternResolver;
    }

    /**
     * 转换本地 markdown 文档
     * @return documents
     */
    public List<Document> loadMarkdownDocuments() {
        List<Document> markdownDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:docs/*.md");
            for (Resource resource : resources) {

                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", resource.getFilename())
                        .build();

                MarkdownDocumentReader documentReader = new MarkdownDocumentReader(resource, config);
                markdownDocuments.addAll(documentReader.get());
            }
        } catch (IOException e) {
            log.error("Load markdown documents failed!");
        }
        return markdownDocuments;
    }
}
