package com.dingzk.dingziaiagent.rag;

import com.dingzk.dingziaiagent.tools.*;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class LizhiToolRegistry {

    @Value("${search-api.apiKey}")
    private String apiKey;

    // MCP 工具
//    @Resource
    private ToolCallbackProvider imageMcpTool;

    @Bean
    public List<ToolCallback> registeredTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebCrawlerTool webCrawlerTool = new WebCrawlerTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        WebDownloadTool webDownloadTool = new WebDownloadTool();
        PdfGeneratorTool pdfGeneratorTool = new PdfGeneratorTool();
        TerminationTool terminationTool = new TerminationTool();

        ToolCallback[] callbacks = ToolCallbacks.from(
                fileOperationTool
                , webSearchTool
                , webCrawlerTool
                , terminalOperationTool
                , webDownloadTool
                , pdfGeneratorTool
                , terminationTool
        );
        List<ToolCallback> toolCallbackList = new ArrayList<>(List.of(callbacks));
        // 注册 MCP 工具
//        toolCallbackList.addAll(List.of(imageMcpTool.getToolCallbacks()));
        return toolCallbackList;
    }
}
