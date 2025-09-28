package com.dingzk.dingziaiagent.rag;

import com.dingzk.dingziaiagent.agent.prompt.JManusPrompt;
import com.dingzk.dingziaiagent.tools.*;
import org.springframework.ai.chat.messages.AssistantMessage;
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

    public static String getToolPrompt(AssistantMessage.ToolCall toolCall) {
        return switch (toolCall.name()) {
            case "executeWebSearch" -> String.format(JManusPrompt.WEB_SEARCH_PROMPT_TEMPLATE, toolCall.arguments());
            case "executeWebCrawl" -> String.format(JManusPrompt.WEB_CRAWLER_PROMPT_TEMPLATE, toolCall.arguments());
            case "executeReadFile" -> JManusPrompt.FILE_READ_PROMPT_TEMPLATE;
            case "executeWriteFile" -> JManusPrompt.FILE_WRITE_PROMPT_TEMPLATE;
            case "executePdfGeneration" -> JManusPrompt.PDF_GENERATE_PROMPT_TEMPLATE;
            case "executeTerminalOperation" -> JManusPrompt.TERMINAL_OPT_PROMPT_TEMPLATE;
            case "doTerminationTool" -> JManusPrompt.TERMINATION_PROMPT_TEMPLATE;
            case "executeWebDownload" -> JManusPrompt.WEB_DOWNLOAD_PROMPT_TEMPLATE;
            default -> throw new IllegalStateException("Unexpected value: " + toolCall.name());
        };
    }
}
