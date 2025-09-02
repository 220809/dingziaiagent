package com.dingzk.dingziaiagent.advisor;

import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;
import java.util.regex.Pattern;

public class SensitiveWordsAdvisor implements BaseAdvisor {

    private static final String sensitiveWordRegex;
    private static final String sensitiveWords;

    private static final Pattern pattern;

    static {
        Resource resource = ResourceUtil.getResourceObj("sensitive-words.txt");
        sensitiveWords = resource.readUtf8Str();
        sensitiveWordRegex = "(" + sensitiveWords.replace(",", "|") + ")";
        pattern = Pattern.compile(sensitiveWordRegex);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userMessageText = chatClientRequest.prompt().getUserMessage().getText();
        if (!pattern.matcher(userMessageText).find()) {
            return chatClientRequest;
        }
        SystemMessage originalSystemMessage = chatClientRequest.prompt().getSystemMessage();
        String newSystemMessage = PromptTemplate.builder()
                .template("""
                        {original} 如果用户准确地输入了如下违禁词：{sensitive}，你应该回复：“抱歉，您的文字中含有敏感词，我无法回答您的问题。”
                        """)
                .variables(Map.of("original", originalSystemMessage.getText(), "sensitive", sensitiveWords))
                .build()
                .render();
        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentSystemMessage(newSystemMessage))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
