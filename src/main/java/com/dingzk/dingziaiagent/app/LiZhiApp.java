package com.dingzk.dingziaiagent.app;

import com.dingzk.dingziaiagent.advisor.LiZhiLoggerAdvisor;
import com.dingzk.dingziaiagent.chatmemory.MySqlChatMemoryRepository;
import com.dingzk.dingziaiagent.mapper.ChatMessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LiZhiApp {
    private final ChatClient chatClient;

    private final String SYSTEM_PROMPT = "你是一名资深情感调解专家，开场表明身份，通过开放式问题邀请用户分享，" +
            "深入探索问题背后的想法与需求，自然将问题导向积极、可行的微小步骤，注意不应评判用户的价值观、选择；若用户有严重自伤/伤人倾向，" +
            "需温和明确地回应：“听起来你的经历十分痛苦，这已经超出了我能帮助的范围，请立即联系专业的心理危机干预机构。”类似表述； " +
            "对话开头的最后自然融入：“我是AI励志师，旨在引导陪伴你；若有严重心理困扰，请线下寻求专业人士帮助。”类似表述。";

    private MessageChatMemoryAdvisor chatMemoryAdvisor;

    @Resource
    private Advisor ragCloudDocumentAdvisor;

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private ToolCallback[] registeredTools;

    public LiZhiApp(ChatModel dashScopeChatModel, ChatMessageMapper chatMessageMapper) {

        MySqlChatMemoryRepository mySqlChatMemoryRepository = MySqlChatMemoryRepository.builder()
                .chatMessageMapper(chatMessageMapper)
                .build();

        ChatMemory mySqlChatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(mySqlChatMemoryRepository)
                .maxMessages(3)
                .build();
        // 想要实现 MySQL 持久化对话记忆，添加此 Advisor
        chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(mySqlChatMemory).build();

        // 使用 MySQL 持久化对话记忆
        chatClient = ChatClient.builder(dashScopeChatModel)
                // 系统 Prompt
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new LiZhiLoggerAdvisor()   // 日志记录 Advisor
//                        , new ReReadingAdvisor()     // Re2 Advisor
//                        , new SensitiveWordsAdvisor()  // 敏感词 Advisor
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(chatMemoryAdvisor)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    public record StructuredReport(String reportName, List<String> suggestions) {}

    public StructuredReport doStructuredChat(String message, String chatId) {
        StructuredReport report = chatClient.prompt()
                // 添加系统 Prompt 内容，引导大模型生成结构化输出
                .system(SYSTEM_PROMPT + "每次对话后，生成建议报告，报告名称为{用户名}建议报告，内容为给出的建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(StructuredReport.class);
        return report;
    }


    /**
     * 知识库问答
     * @param message 用户消息
     * @param conversationId 对话Id
     * @return 大模型返回内容
     */
    public String doRagChat(String message, String conversationId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
//                .advisors(ragCloudDocumentAdvisor)   // 云知识库文档检索
                .advisors(new QuestionAnswerAdvisor(pgVectorStore))   // 向量数据库文档检索
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    /**
     * 使用工具调用
     * @param message 用户消息
     * @param conversationId 对话 id
     * @return result
     */
    public String doChatWithTools(String message, String conversationId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolCallbacks(registeredTools)
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();
    }

    @Resource
    private ToolCallbackProvider imageMcpTool;

    /**
     * MCP
     * @param message 消息
     * @param conversationId 对话 id
     * @return 返回消息
     */
    public String doChatWithMcp(String message, String conversationId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .toolCallbacks(imageMcpTool)
                .call()
                .chatResponse();

        return response.getResult().getOutput().getText();
    }
}
