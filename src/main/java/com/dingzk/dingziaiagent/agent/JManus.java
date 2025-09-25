package com.dingzk.dingziaiagent.agent;

import com.dingzk.dingziaiagent.advisor.LiZhiLoggerAdvisor;
import com.dingzk.dingziaiagent.agent.prompt.JManusPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JManus extends ToolCallAgent{
    public JManus(List<ToolCallback> registeredTools, ChatModel dashscopeChatModel) {
        super(registeredTools);
        setName("JManus");
        setSystemPrompt(JManusPrompt.SYSTEM_PROMPT);
        setNextStepPrompt(JManusPrompt.NEXT_STEP_PROMPT);
        setMaxAttemptStep(20);
        setChatClient(ChatClient.builder(dashscopeChatModel)
//                .defaultAdvisors(new LiZhiLoggerAdvisor())
                .build());

    }
}
