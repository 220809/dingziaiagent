package com.dingzk.dingziaiagent.app;

import cn.hutool.core.io.resource.ResourceUtil;
import com.dingzk.dingziaiagent.advisor.LiZhiLoggerAdvisor;
import com.dingzk.dingziaiagent.advisor.SensitiveWordsAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class LiZhiApp {
    private final ChatClient chatClient;

    private final String SYSTEM_PROMPT = "你是一名资深情感调解专家，开场表明身份，通过开放式问题邀请用户分享，" +
            "深入探索问题背后的想法与需求，自然将问题导向积极、可行的微小步骤，注意不应评判用户的价值观、选择；若用户有严重自伤/伤人倾向，" +
            "需温和明确地回应：“听起来你的经历十分痛苦，这已经超出了我能帮助的范围，请立即联系专业的心理危机干预机构。”类似表述； " +
            "对话开头的最后自然融入：“我是AI励志师，旨在引导陪伴你；若有严重心理困扰，请线下寻求专业人士帮助。”类似表述。";

    private final ChatModel chatModel;

    private final BeanOutputConverter<StructuredReport> beanOutputConverter;

    public LiZhiApp(ChatModel dashScopeChatModel, ChatMemory mySqlChatMemory) {
        this.chatModel = dashScopeChatModel;
        beanOutputConverter = new BeanOutputConverter<>(StructuredReport.class);

        // 使用 MySQL 持久化对话记忆
        chatClient = ChatClient.builder(dashScopeChatModel)
                // 系统 Prompt
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(mySqlChatMemory).build()
                        , new LiZhiLoggerAdvisor()   // 日志记录 Advisor
//                        , new ReReadingAdvisor()     // Re2 Advisor
                        , new SensitiveWordsAdvisor()  // 敏感词 Advisor
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId))
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
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId))
                .call()
                .entity(StructuredReport.class);
        return report;
    }

    public StructuredReport doStructuredChatUsingModel(String message, String chatId) {
        String format = this.beanOutputConverter.getFormat();

        Message systemMessage = SystemMessage.builder()
                .text(SYSTEM_PROMPT +
                        "每次对话后，生成建议报告，报告名称为{用户名}建议报告，内容为给出的建议列表。"
                        + "格式为: " + format   // 需要在消息中指示格式，否则会将无法解析（可能是企图将整段 Message 作为格式）
                )
                .build();
        Message userMessage = UserMessage.builder()
                .text(message)
                .build();

        Prompt prompt = Prompt.builder()
                .messages(systemMessage, userMessage)
                .build();

        ChatResponse chatResponse = chatModel.call(prompt);

        String response = chatResponse.getResult().getOutput().getText();
        StructuredReport report = this.beanOutputConverter.convert(response);
        log.info("AI Response: {}", response);
        return report;
    }
}
