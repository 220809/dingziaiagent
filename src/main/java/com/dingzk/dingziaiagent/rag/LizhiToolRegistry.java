package com.dingzk.dingziaiagent.rag;

import com.dingzk.dingziaiagent.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LizhiToolRegistry {

    @Value("${search-api.apiKey}")
    private String apiKey;

    @Bean
    public ToolCallback[] registeredTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebCrawlerTool webCrawlerTool = new WebCrawlerTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        WebDownloadTool webDownloadTool = new WebDownloadTool();
        PdfGeneratorTool pdfGeneratorTool = new PdfGeneratorTool();

        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webCrawlerTool,
                terminalOperationTool,
                webDownloadTool,
                pdfGeneratorTool
        );
    }
}
